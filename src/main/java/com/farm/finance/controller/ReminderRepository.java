package com.farm.finance.repository;

import com.farm.finance.model.Reminder;
import com.farm.finance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    // Find all reminders for a user (not completed)
    List<Reminder> findByUserAndCompletedFalse(User user);

    // Find all reminders for a user, ordered by date
    List<Reminder> findByUserOrderByReminderDateAsc(User user);
}