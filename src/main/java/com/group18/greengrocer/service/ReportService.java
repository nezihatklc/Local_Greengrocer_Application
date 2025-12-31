package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.CarrierRatingDAO;
import com.group18.greengrocer.dao.OrderDAO;
import com.group18.greengrocer.dao.ProductDAO;
import com.group18.greengrocer.dao.UserDAO;
import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.model.ReportData;
import com.group18.greengrocer.model.Role;
import com.group18.greengrocer.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * ReportService
 * Business logic for generating OWNER reports.
 *
 * Rules:
 * - Controllers must NOT run SQL.
 * - DAO does raw DB operations.
 * - Service aggregates/transforms data for the UI.
 */
public class ReportService {

    private static final int SALES_REPORT_DAYS = 30;

    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final CarrierRatingDAO carrierRatingDAO;
    private final UserDAO userDAO;

    public ReportService() {
        this.orderDAO = new OrderDAO();
        this.productDAO = new ProductDAO();
        this.carrierRatingDAO = new CarrierRatingDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Generates a report on total sales revenue (Last 30 days).
     *
     * @return list of daily sales data points
     */
    public List<ReportData> generateSalesReport() {
        // NOTE: This requires OrderDAO.getDailySales(int days).
        return orderDAO.getDailySales(SALES_REPORT_DAYS);
    }

    /**
     * Generates a report on current stock levels.
     *
     * @return list of product stock data points
     */
    public List<ReportData> generateStockReport() {
        List<ReportData> data = new ArrayList<>();
        List<Product> products = productDAO.findAll();

        for (Product p : products) {
            data.add(new ReportData(p.getName(), p.getStock()));
        }
        return data;
    }

    /**
     * Generates a report on carrier performance (average ratings).
     *
     * @return list of carrier rating data points
     */
    public List<ReportData> generateCarrierPerformanceReport() {
        List<ReportData> data = new ArrayList<>();
        List<User> carriers = userDAO.findUsersByRole(Role.CARRIER);

        for (User carrier : carriers) {
            double avgRating = carrierRatingDAO.getAverageRatingForCarrier(carrier.getId());
            data.add(new ReportData(carrier.getUsername(), avgRating));
        }
        return data;
    }

    /**
     * Generates an income report.
     * Currently same as sales report, can be extended later.
     *
     * @return list of income data points
     */
    public List<ReportData> generateIncomeReport() {
        return generateSalesReport();
    }
}
