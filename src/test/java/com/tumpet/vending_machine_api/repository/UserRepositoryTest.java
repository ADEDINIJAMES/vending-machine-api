package com.tumpet.vending_machine_api.repository;

import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.model.Users;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class UserRepositoryTest {
    @Autowired
private TestEntityManager entityManager;

    @MockBean
    private JavaMailSender mailSender;
    @MockBean
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private UserRepository userRepository;
    Users mockUser = new Users();


    @BeforeEach
        void setUp(){
        mockUser.setUsername("adedinijames28@gmail.com");
        mockUser.setFirstName("James");
        mockUser.setLastName("James");
        mockUser.setPassword("Pass");
        mockUser.setRole(Role.BUYER);
        mockUser.setEmail("adedinijames28@gmail.com");
        entityManager.persist(mockUser);
        entityManager.flush();
    }

@Test
    void testFindByUsername (){


    Optional<Users>user =userRepository.findByUsername(mockUser.getUsername());
    Assertions.assertNotNull(user);
    Assertions.assertTrue(user.isPresent());


}

@Test
    void testExistByEmail (){


    Boolean isExist = userRepository.existsByEmail("adedinijames28@gmail.com");

    Assertions.assertTrue(isExist);

}

    @Test
    void testIsExistByUsername (){

        Boolean isExist = userRepository.existsByUsername("adedinijames28@gmail.com");

        Assertions.assertTrue(isExist);
    }






}