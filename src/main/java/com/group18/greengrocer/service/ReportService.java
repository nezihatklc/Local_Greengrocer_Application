package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.ReportDAO;
import com.group18.greengrocer.model.ReportData;
import java.util.List;

public class ReportService {

    private static ReportService instance;
    private ReportDAO reportDAO;

    private ReportService() {
        this.reportDAO = ReportDAO.getInstance();
    }

    public static synchronized ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }

    public List<ReportData> getRevenueByProduct() {
        return reportDAO.getRevenueByProduct();
    }

    public List<ReportData> getOrdersByStatus() {
        return reportDAO.getOrdersByStatus();
    }

    public List<ReportData> getMonthlyRevenue() {
        return reportDAO.getMonthlyRevenue();
    }
}
