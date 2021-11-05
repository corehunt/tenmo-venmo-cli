package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcUserDao implements UserDao {

    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");
    private JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int findIdByUsername(String username) {
        String sql = "SELECT user_id FROM users WHERE username ILIKE ?;";
        Integer id = jdbcTemplate.queryForObject(sql, Integer.class, username);
        if (id != null) {
            return id;
        } else {
            return -1;
    }
    }
    @Override
    public int getAccountNumber(Long userId){
        String sql = "SELECT account_id FROM accounts WHERE user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql,userId);
        int accountId = -1;
        if(results.next()){
            accountId = results.getInt("account_id");
            return accountId;
        }
        return accountId;
    }
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash FROM users;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            User user = mapRowToUser(results);
            users.add(user);
        }
        return users;
    }

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        String sql = "SELECT user_id, username, password_hash FROM users WHERE username ILIKE ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        if (rowSet.next()){
            return mapRowToUser(rowSet);
            }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    @Override
    public boolean create(String username, String password) {

        // create user
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        Integer newUserId;
        try {
            newUserId = jdbcTemplate.queryForObject(sql, Integer.class, username, password_hash);
        } catch (DataAccessException e) {
            return false;
                }

        // create account
        sql = "INSERT INTO accounts (user_id, balance) values(?, ?)";
        try {
            jdbcTemplate.update(sql, newUserId, STARTING_BALANCE);
        } catch (DataAccessException e) {
            return false;
        }

        return true;
    }
    @Override
    public BigDecimal getBalance(Long userId){
        BigDecimal userBalance = new BigDecimal(0.00);
        String sql = "SELECT balance FROM accounts JOIN users USING(user_id) WHERE user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql,userId);
        if(results.next()){
            userBalance = results.getBigDecimal("balance");
        }

        return userBalance;
    }

    @Override
        public Transfer createSend(Long currentUser, Long receivingUser, BigDecimal amount){
        String sql = "INSERT INTO transfers (transfer_type_id,transfer_status_id,account_from,account_to,amount) VALUES (2,2,?,?,?) RETURNING transfer_id;";
        Transfer transfer = new Transfer();
        try {
            transfer = jdbcTemplate.queryForObject(sql, Transfer.class, getAccountNumber(currentUser), getAccountNumber(receivingUser), amount);
        }catch (DataAccessException e){

        }

        return transfer;
    }

    @Override
    public List<Transfer> transferHistory(User user) {
        List<Transfer> listOfTransfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                     "FROM transfers " +
                     "JOIN accounts ON account_from = account_id " +
                     "ORDER BY user_id = ?";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, user.getId());
        while(results.next()) {
            listOfTransfers.add(mapRowToTransfer(results));
        }


                sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                        "FROM transfers " +
                        "JOIN accounts ON account_to = account_id " +
                        "WHERE user_id = ? " +
                        "ORDER BY user_id = ?";
                results = jdbcTemplate.queryForRowSet(sql, user.getId(), user.getId());
                while (results.next()) {
                    listOfTransfers.add(mapRowToTransfer(results));
                }
                return listOfTransfers;
            }

    private Transfer mapRowToTransfer(SqlRowSet rowSet) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rowSet.getInt("transfer_id"));
        transfer.setTransferTypeId(rowSet.getInt("transfer_type_id"));
        transfer.setTransferStatusId(rowSet.getInt("transfer_status_id"));
        transfer.setAccountFrom(rowSet.getInt("account_from"));
        transfer.setAccountTo(rowSet.getInt("account_to"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));
        return transfer;
    }
    @Override
    public void decreaseBalance(Long userId, BigDecimal amount){
        System.out.println(amount);
        String sql = "UPDATE accounts SET balance = ? WHERE user_id = ?";
        BigDecimal balanceToChange = getBalance(userId);
        jdbcTemplate.update(sql,balanceToChange.subtract(amount).doubleValue(), userId);
    }
    @Override
    public void increaseBalance(Long userId, BigDecimal amount){
        String sql = "UPDATE accounts SET balance = ? WHERE user_id = ?";
        BigDecimal balanceToChange = getBalance(userId);
        jdbcTemplate.update(sql,balanceToChange.add(amount), userId);
    }

    private User mapRowToUser (SqlRowSet rs){
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("USER");
        return user;
    }


}
