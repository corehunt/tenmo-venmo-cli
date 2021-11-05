package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface UserDao {

    List<User> findAll();

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean create(String username, String password);

    BigDecimal getBalance(Long userId);

    Transfer createSend(Long currentUser, Long receivingUser, BigDecimal amount);

    int getAccountNumber(Long userId);

    List<Transfer> transferHistory(User user);

    void decreaseBalance(Long userId, BigDecimal amount);

    void increaseBalance(Long userId, BigDecimal amount);
}
