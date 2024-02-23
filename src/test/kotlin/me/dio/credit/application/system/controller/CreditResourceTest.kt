package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.dto.request.CustomerUpdateDto
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {
    @Autowired private lateinit var creditRepository: CreditRepository
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/credit"
    }

    @BeforeEach
    fun setup() = creditRepository.deleteAll()

    @AfterEach
    fun tearDown() = creditRepository.deleteAll()

        @Test
        fun `should create a customer and return 201 status`() {
            //given
            val customerDto: CustomerDto = buildCustomerDto()
            val valueAsString: String = objectMapper.writeValueAsString(customerDto)
            //when
            //then
            mockMvc.perform(
                MockMvcRequestBuilders.post(URL)
                    .content(valueAsString)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Art"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Vil"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("02150463732"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("art@email.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.income").value("1000.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("12345"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua São Miguel"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        fun `should not save a customer with same CPF and return 409 status`() {
            //given
            customerRepository.save(buildCustomerDto().toEntity())
            val customerDto: CustomerDto = buildCustomerDto()
            val valueAsString: String = objectMapper.writeValueAsString(customerDto)
            //when
            //then
            mockMvc.perform(
                MockMvcRequestBuilders.post(URL)
                    .content(valueAsString)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.title")
                    .value("Conflict! Consult the documentation")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(409))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.dao.DataIntegrityViolationException")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        fun `should not save a customer with firstName empty and return 400 status`() {
            //given
            val customerDto: CustomerDto = buildCustomerDto(firstName = "")
            val valueAsString: String = objectMapper.writeValueAsString(customerDto)
            //when
            //then
            mockMvc.perform(
                MockMvcRequestBuilders.post(URL)
                    .content(valueAsString)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.title")
                    .value("Bad Request! Consult the documentation")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.web.bind.MethodArgumentNotValidException")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        fun `should find customer by id and return 200 status`() {
            //given
            val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
            //when
            //then
            mockMvc.perform(
                MockMvcRequestBuilders.get("$URL/${customer.id}")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Art"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Vil"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("02150463732"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("art@email.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.income").value("1000.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("12345"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua São Miguel"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        fun `should not find customer with invalid id and return 400 status`() {
            //given
            val invalidId: Long = 2L
            //when
            //then
            mockMvc.perform(
                MockMvcRequestBuilders.get("$URL/$invalidId")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.title")
                    .value("Bad Request! Consult the documentation")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        fun `should delete customer by id and return 204 status`() {
            //given
            val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
            //when
            //then
            mockMvc.perform(
                MockMvcRequestBuilders.delete("$URL/${customer.id}")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        fun `should not delete customer by id and return 400 status`(){
            //given
            val invalidId: Long = Random().nextLong()
            //when
            //then
            mockMvc.perform(
                MockMvcRequestBuilders.delete("$URL/${invalidId}")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        fun `should update a customer and return 200 status`() {
            //given
            val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
            val customerUpdateDto: CustomerUpdateDto = builderCustomerUpdateDto()
            val valueAsString: String = objectMapper.writeValueAsString(customerUpdateDto)
            //when
            //then
            mockMvc.perform(
                MockMvcRequestBuilders.patch("URL?customerId=${customer.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(valueAsString)
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("ArtUpdate"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("VilUpdate"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("02150463732"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("art@email.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.income").value("5000.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("67890"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua São Miguel, 1528"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        fun `should not update a customer with invalid id and return 400 status`() {
            //given
            val invalidId: Long = Random().nextLong()
            val customerUpdateDto: CustomerUpdateDto = builderCustomerUpdateDto()
            val valueAsString: String = objectMapper.writeValueAsString(customerUpdateDto)
            //when
            //then
            mockMvc.perform(
                MockMvcRequestBuilders.patch("URL?customerId=$invalidId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(valueAsString)
            )
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
                .andDo(MockMvcResultHandlers.print())
        }

        private fun buildCustomerDto(
            firstName: String = "Art",
            lastName: String = "Vil",
            cpf: String = "02150463732",
            email: String = "art@email.com",
            income: BigDecimal = BigDecimal.valueOf(1000.0),
            password: String = "00000",
            zipCode: String = "12345",
            street: String = "Rua São Miguel"
        ) = CustomerDto(
            firstName = firstName,
            lastName = lastName,
            cpf = cpf,
            email = email,
            income = income,
            password = password,
            zipCode = zipCode,
            street = street
        )

        private fun builderCustomerUpdateDto(
            firstName: String = "ArtUpdate",
            lastName: String = "VilUpdate",
            income: BigDecimal = BigDecimal.valueOf(5000.0),
            zipCode: String = "67890",
            street: String = "Rua São Miguel, 1528"
        ): CustomerUpdateDto = CustomerUpdateDto(
            firstName = firstName,
            lastName = lastName,
            income = income,
            zipCode = zipCode,
            street = street
        )
    }
}