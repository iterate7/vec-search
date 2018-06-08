package util;

public class Util {

	public static double[] product(final double x, final double[] v) {
        double[] r = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            r[i] = x * v[i];
        }
        return r;
    }

	public static double[] sub(final double[] a, final double[] b) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            r[i] = a[i] - b[i];
        }
        return r;
    }

	public static void normalize(final double[] vector) {
        double norm = norm(vector);
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }

    }

    /**
     * Returns the norm L2. sqrt(sum_i(v_i^2))
     * @param v
     * @return
     */
	public static double norm(final double[] v) {
        double agg = 0;

        for (int i = 0; i < v.length; i++) {
            agg += (v[i] * v[i]);
        }

        return Math.sqrt(agg);
    }

    public static double dotProduct(final double[] v1, final double[] v2) {
        double agg = 0;

        for (int i = 0; i < v1.length; i++) {
            agg += (v1[i] * v2[i]);
        }

        return agg;
    }

}
