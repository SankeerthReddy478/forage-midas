package com.jpmc.midascore.incentives;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IncentiveClient {

    @Autowired
    private RestTemplate restTemplate;

    public Incentive getIncentive(Transaction transaction) {
        return restTemplate.postForObject(
                "http://localhost:8080/incentive",
                transaction,
                Incentive.class);
    }
}
