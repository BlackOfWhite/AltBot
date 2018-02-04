package org.logic.utils.regression;

import org.apache.log4j.Logger;

import java.util.List;

public class LinearRegression {

    private static final Logger logger = Logger.getLogger(LinearRegression.class);

    public static double linearRegression(List<Point> points) {
        int n = 0;
        int MAXN = points.size();
        double[] x = new double[MAXN];
        double[] y = new double[MAXN];

        // first pass: read in data, compute xbar and ybar
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (Point point : points) {
            x[n] = point.getX();
            y[n] = point.getY();
            sumx += x[n];
            sumx2 += x[n] * x[n];
            sumy += y[n];
            n++;
        }
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        double beta1 = xybar / xxbar;
        double beta0 = ybar - beta1 * xbar;

        // print results
        logger.debug("y = " + beta1 + " * x + " + beta0);
        return beta1;
//        // analyze results
//        int df = n - 2;
//        double rss = 0.0;      // residual sum of squares
//        double ssr = 0.0;      // regression sum of squares
//        for (int i = 0; i < n; i++) {
//            double fit = beta1 * x[i] + beta0;
//            rss += (fit - y[i]) * (fit - y[i]);
//            ssr += (fit - ybar) * (fit - ybar);
//        }
//        double R2 = ssr / yybar;
//        double svar = rss / df;
//        double svar1 = svar / xxbar;
//        double svar0 = svar / n + xbar * xbar * svar1;
////        logger.debug("R^2                 = " + R2);
////        StdOut.println("std error of beta_1 = " + Math.sqrt(svar1));
////        StdOut.println("std error of beta_0 = " + Math.sqrt(svar0));
//        svar0 = svar * sumx2 / (n * xxbar);
////        StdOut.println("std error of beta_0 = " + Math.sqrt(svar0));
//
//        logger.debug("SSTO = " + yybar);
//        logger.debug("SSE  = " + rss);
//        logger.debug("SSR  = " + ssr);
//        logger.debug("svar = " + svar);
//        logger.debug("svar0 = " + svar0);
//        logger.debug("svar1 = " + svar1);
    }
}