package com.subtrackr.service;

import com.subtrackr.model.Subscription;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    public List<String> generateRecommendations(List<Subscription> subscriptions, double income) {
        List<String> recommendations = new ArrayList<>();
        
        int streamingCount = 0;
        double streamingCost = 0;
        double totalSpend = 0;
        String categoryTracker = null;
        boolean allSameCategory = true;
        
        for (Subscription s : subscriptions) {
            if ("active".equals(s.getStatus()) || "Active".equalsIgnoreCase(s.getStatus())) {
                double monthlyAmount = s.getAmount();
                if ("yearly".equalsIgnoreCase(s.getBillingCycle())) {
                    monthlyAmount /= 12.0;
                    recommendations.add("Your " + s.getName() + " subscription bills yearly. Make sure you have ₹" + 
                        String.format("%.2f", s.getAmount()) + " set aside for " + s.getRenewalDate() + ".");
                } else if ("weekly".equalsIgnoreCase(s.getBillingCycle())) {
                    monthlyAmount *= 4.33;
                }
                
                totalSpend += monthlyAmount;
                
                if ("streaming".equalsIgnoreCase(s.getCategory())) {
                    streamingCount++;
                    streamingCost += monthlyAmount;
                }
                
                if (monthlyAmount > 1000) {
                    recommendations.add("Your " + s.getName() + " subscription at ₹" + String.format("%.2f", monthlyAmount) + 
                        "/month is a significant expense. Review if you're getting full value.");
                }
                
                if (categoryTracker == null) {
                    categoryTracker = s.getCategory();
                } else if (!categoryTracker.equalsIgnoreCase(s.getCategory())) {
                    allSameCategory = false;
                }
            }
        }
        
        if (streamingCount >= 2) {
            recommendations.add("You have " + streamingCount + " streaming services totalling ₹" + 
                String.format("%.2f", streamingCost) + "/month. Consider cancelling one you use least.");
        }
        
        if (income > 0) {
            double percentOfIncome = (totalSpend / income) * 100;
            if (totalSpend > income) {
                recommendations.add("⚠️ Your subscription costs exceed your stated income. Immediate review recommended.");
            } else if (percentOfIncome > 30) {
                recommendations.add("Your subscriptions consume " + String.format("%.1f", percentOfIncome) + 
                    "% of your income. Financial experts recommend staying under 10%.");
            }
        }
        
        if (!subscriptions.isEmpty() && allSameCategory && categoryTracker != null) {
            recommendations.add("All your subscriptions are " + categoryTracker + ". Consider diversifying or cutting back.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Great job! Your subscription spending looks healthy. Keep monitoring with SubTrackr.");
        }
        
        return recommendations;
    }
}
