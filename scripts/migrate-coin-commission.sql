-- ============================================================
-- Coin/Commission 분리 마이그레이션 (Issue #234)
-- ============================================================
-- 목적:
--   Product STI 에 섞여 있던 CoinProduct/CommissionProduct 를
--   독립 도메인(coin_packages, commission_policies)으로 분리.
--   payments 는 product_id 대신 (target_type, target_id) 디스크리미네이터 사용.
--
-- 실행 순서:
--   1) coin_packages, commission_policies 신규 테이블 생성
--   2) 기존 products(CoinProduct/CommissionProduct) → 각 신규 테이블로 데이터 이관
--   3) payments 에 target_type/target_id 컬럼 추가 + 기존 product_id 로부터 백필
--   4) commissions.product_id → commission_policy_id 로 리네임
--   5) products 에서 COIN/COMMISSION 로우 제거 (ddl-auto=update 는 컬럼/테이블을
--      자동 제거하지 않으므로 dtype/coin_amount/coin_cost 컬럼은 수동 정리 필요 시 DBA 가 수행)
--
-- 주의:
--   - 이 스크립트는 실행 전 payments/products/commissions 의 기존 데이터가
--     모두 유효한 FK 를 갖고 있다고 가정한다.
--   - 운영 적용 전 dev 에서 예행 실행을 권장.
-- ============================================================

START TRANSACTION;

-- ------------------------------------------------------------
-- 1. 신규 테이블
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS coin_packages (
    id            VARCHAR(26)  NOT NULL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    price_won     INT          NOT NULL,
    coin_amount   INT          NOT NULL,
    active        TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL
);

CREATE TABLE IF NOT EXISTS commission_policies (
    id            VARCHAR(26)  NOT NULL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    coin_cost     INT          NOT NULL,
    active        TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL
);

-- ------------------------------------------------------------
-- 2. 기존 products → 신규 테이블 백필
--    (dtype 은 Product STI 의 discriminator 컬럼)
-- ------------------------------------------------------------
INSERT INTO coin_packages (id, name, price_won, coin_amount, active, created_at, updated_at)
SELECT id, name, price_won, coin_amount, active, created_at, updated_at
FROM products
WHERE dtype = 'CoinProduct';

INSERT INTO commission_policies (id, name, coin_cost, active, created_at, updated_at)
SELECT id, name, coin_cost, active, created_at, updated_at
FROM products
WHERE dtype = 'CommissionProduct';

-- ------------------------------------------------------------
-- 3. payments 에 target_type/target_id 추가 + 백필
-- ------------------------------------------------------------
ALTER TABLE payments
    ADD COLUMN target_type VARCHAR(30) NULL AFTER user_id,
    ADD COLUMN target_id   VARCHAR(26) NULL AFTER target_type;

-- 기존 결제의 product 타입에 따라 target_type 결정
UPDATE payments p
JOIN products pr ON pr.id = p.product_id
SET p.target_type = CASE pr.dtype
        WHEN 'CoinProduct'   THEN 'COIN_PACKAGE'
        WHEN 'CourseProduct' THEN 'COURSE_PRODUCT'
    END,
    p.target_id = pr.id
WHERE pr.dtype IN ('CoinProduct', 'CourseProduct');

-- NOT NULL 로 승격 (백필 누락 건이 있으면 여기서 실패하도록 둔다)
ALTER TABLE payments
    MODIFY COLUMN target_type VARCHAR(30) NOT NULL,
    MODIFY COLUMN target_id   VARCHAR(26) NOT NULL;

-- product_id 컬럼은 JPA 엔티티에서 제거됐지만 ddl-auto=update 는 drop 하지 않음.
-- 필요 시 DBA 가 수동으로 DROP. 예:
--   ALTER TABLE payments DROP COLUMN product_id;

-- ------------------------------------------------------------
-- 4. commissions.product_id → commission_policy_id 리네임
-- ------------------------------------------------------------
ALTER TABLE commissions
    CHANGE COLUMN product_id commission_policy_id VARCHAR(26) NOT NULL;

-- 기존 인덱스(idx_commissions_product)가 있다면 정리하고 신규 인덱스 생성
-- (인덱스명이 환경마다 다를 수 있어 존재 시에만 실행)
-- 예: ALTER TABLE commissions DROP INDEX idx_commissions_product;
CREATE INDEX idx_commissions_policy ON commissions (commission_policy_id);

-- ------------------------------------------------------------
-- 5. products 에서 CoinProduct / CommissionProduct 로우 제거
-- ------------------------------------------------------------
DELETE FROM products WHERE dtype IN ('CoinProduct', 'CommissionProduct');

COMMIT;

-- ============================================================
-- 후속 수동 작업 (DBA 가 별도로 판단 후 실행)
-- ============================================================
-- ALTER TABLE payments DROP COLUMN product_id;
-- ALTER TABLE products DROP COLUMN coin_amount;
-- ALTER TABLE products DROP COLUMN coin_cost;
-- 위 3건은 ddl-auto=update 가 자동으로 drop 해주지 않으므로 남게 된다.
-- 완전히 clean up 하려면 운영 반영 시점 이후 수행할 것.
