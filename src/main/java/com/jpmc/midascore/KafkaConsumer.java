package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.incentives.IncentiveClient;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRecordRepository recordRepository;

    @Autowired
    private IncentiveClient incentiveClient;

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core")
    public void listen(Transaction transaction) {
        UserRecord sender = userRepository.findById(transaction.getSenderId()).orElse(null);
        UserRecord recipient = userRepository.findById(transaction.getRecipientId()).orElse(null);

        // Basic validation
        if (sender == null || recipient == null) return;
        if (sender.getBalance() < transaction.getAmount()) return;

        // Call Incentive API
        Incentive incentiveResponse = incentiveClient.getIncentive(transaction);
        float incentive = (incentiveResponse != null) ? incentiveResponse.getAmount() : 0f;

        // Update balances
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentive);

        // Record transaction
        TransactionRecord record = new TransactionRecord();
        record.setSender(sender);
        record.setRecipient(recipient);
        record.setAmount(transaction.getAmount());
        record.setIncentive(incentive);
        recordRepository.save(record);

        // Save both users
        userRepository.save(sender);
        userRepository.save(recipient);
    }
}