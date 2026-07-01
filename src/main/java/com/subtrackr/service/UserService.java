package com.subtrackr.service;

import com.subtrackr.model.User;
import com.subtrackr.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.subtrackr.repository.SubscriptionRepository;
import com.subtrackr.repository.PaymentRepository;
import com.subtrackr.repository.ReminderSentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final ReminderSentRepository reminderSentRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       SubscriptionRepository subscriptionRepository,
                       PaymentRepository paymentRepository,
                       ReminderSentRepository reminderSentRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.reminderSentRepository = reminderSentRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        // Hash password with BCrypt for database compatibility
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);
        
        if (user.getPlan() == null) {
            user.setPlan("basic");
        }
        if (user.getRole() == null) {
            user.setRole("user");
        }
        if (user.getStatus() == null) {
            user.setStatus("active");
        }
        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isPresent()) {
            User user = optUser.get();
            if (BCrypt.checkpw(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    @Transactional
    public boolean updateIncome(int userId, double income) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setIncome(income);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updatePlan(int userId, String plan) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setPlan(plan);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public User updateProfile(int userId, String name, String email) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            if (!user.getEmail().equalsIgnoreCase(email)) {
                Optional<User> existing = userRepository.findByEmail(email);
                if (existing.isPresent()) {
                    throw new IllegalArgumentException("Email already taken by another user");
                }
            }
            user.setFullName(name);
            user.setEmail(email);
            return userRepository.save(user);
        }
        throw new IllegalArgumentException("User not found");
    }

    @Transactional
    public void updatePassword(int userId, String currentPassword, String newPassword) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("Incorrect current password");
            }
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            userRepository.save(user);
            return;
        }
        throw new IllegalArgumentException("User not found");
    }

    @Transactional
    public boolean updateStatus(int userId, String status) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setStatus(status);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public void deleteUser(int userId) {
        for (com.subtrackr.model.Subscription sub : subscriptionRepository.findAll()) {
            if (sub.getUserId() == userId) {
                reminderSentRepository.deleteBySubscriptionId(sub.getId());
            }
        }
        subscriptionRepository.deleteByUserId(userId);
        paymentRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }
}
