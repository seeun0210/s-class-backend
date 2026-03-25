package com.sclass.domain.domains.user.adaptor

import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.exception.UserNotFoundException
import com.sclass.domain.domains.user.repository.UserRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class UserAdaptorTest {
    private lateinit var userRepository: UserRepository
    private lateinit var userAdaptor: UserAdaptor

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        userAdaptor = UserAdaptor(userRepository)
    }

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 유저를 반환한다`() {
            val user = mockk<User>()
            every { userRepository.findById("user-id") } returns Optional.of(user)

            val result = userAdaptor.findById("user-id")

            assertEquals(user, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 UserNotFoundException이 발생한다`() {
            every { userRepository.findById("unknown-id") } returns Optional.empty()

            assertThrows<UserNotFoundException> {
                userAdaptor.findById("unknown-id")
            }
        }
    }

    @Nested
    inner class FindByEmail {
        @Test
        fun `존재하는 이메일로 조회하면 유저를 반환한다`() {
            val user = mockk<User>()
            every { userRepository.findByEmail("test@example.com") } returns user

            val result = userAdaptor.findByEmail("test@example.com")

            assertEquals(user, result)
        }

        @Test
        fun `존재하지 않는 이메일로 조회하면 UserNotFoundException이 발생한다`() {
            every { userRepository.findByEmail("unknown@example.com") } returns null

            assertThrows<UserNotFoundException> {
                userAdaptor.findByEmail("unknown@example.com")
            }
        }
    }

    @Nested
    inner class FindByEmailOrNull {
        @Test
        fun `존재하지 않는 이메일로 조회하면 null을 반환한다`() {
            every { userRepository.findByEmail("unknown@example.com") } returns null

            val result = userAdaptor.findByEmailOrNull("unknown@example.com")

            assertNull(result)
        }
    }

    @Nested
    inner class FindByOauthId {
        @Test
        fun `존재하지 않는 oauthId로 조회하면 null을 반환한다`() {
            every {
                userRepository.findByOauthIdAndAuthProvider("unknown-id", AuthProvider.GOOGLE)
            } returns null

            val result = userAdaptor.findByOauthId("unknown-id", AuthProvider.GOOGLE)

            assertNull(result)
        }
    }

    @Nested
    inner class ExistsByEmail {
        @Test
        fun `존재하는 이메일이면 true를 반환한다`() {
            every { userRepository.existsByEmail("test@example.com") } returns true

            val result = userAdaptor.existsByEmail("test@example.com")

            assertTrue(result)
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `유저 저장을 repository에 위임한다`() {
            val user = mockk<User>()
            every { userRepository.save(user) } returns user

            val result = userAdaptor.save(user)

            assertEquals(user, result)
            verify { userRepository.save(user) }
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `유저 삭제를 repository에 위임한다`() {
            every { userRepository.deleteById("user-id") } just runs

            userAdaptor.delete("user-id")

            verify { userRepository.deleteById("user-id") }
        }
    }
}
