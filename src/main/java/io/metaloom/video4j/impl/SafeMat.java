package io.metaloom.video4j.impl;

import io.metaloom.opencv.core.Mat;
import io.metaloom.opencv.core.Point;
import io.metaloom.opencv.core.Range;
import io.metaloom.opencv.core.Rect;
import io.metaloom.opencv.core.Scalar;
import io.metaloom.opencv.core.Size;

/**
 * Wrapper for a {@link Mat} which provides {@link AutoCloseable} implementation to ensure that the user handles {@link #close()} / {@link #release()}.
 */
public class SafeMat implements AutoCloseable {

	private final Mat mat;

	public SafeMat() {
		this.mat = MatProvider.mat();
	}

	@Override
	public void close() {
		mat.release();
	}

	public Mat delegate() {
		return mat;
	}

	public int hashCode() {
		return mat.hashCode();
	}

	public int channels() {
		return mat.channels();
	}

	public int checkVector(int elemChannels, int depth, boolean requireContinuous) {
		return mat.checkVector(elemChannels, depth, requireContinuous);
	}

	public int checkVector(int elemChannels) {
		return mat.checkVector(elemChannels);
	}

	public boolean equals(Object obj) {
		return mat.equals(obj);
	}

	public Mat clone() {
		return mat.clone();
	}

	public Mat col(int x) {
		return mat.col(x);
	}

	public Mat colRange(int startcol, int endcol) {
		return mat.colRange(startcol, endcol);
	}

	public Mat colRange(Range r) {
		return mat.colRange(r);
	}

	public int dims() {
		return mat.dims();
	}

	public int cols() {
		return mat.cols();
	}

	public void convertTo(Mat m, int rtype, double alpha, double beta) {
		mat.convertTo(m, rtype, alpha, beta);
	}

	public void convertTo(Mat m, int rtype) {
		mat.convertTo(m, rtype);
	}

	public void copyTo(Mat m) {
		mat.copyTo(m);
	}

	public void copyTo(Mat m, Mat mask) {
		mat.copyTo(m, mask);
	}

	public int depth() {
		return mat.depth();
	}

	public double dot(Mat m) {
		return mat.dot(m);
	}

	public long elemSize() {
		return mat.elemSize();
	}

	public long elemSize1() {
		return mat.elemSize1();
	}

	public boolean empty() {
		return mat.empty();
	}

	public Mat inv(int method) {
		return mat.inv(method);
	}

	public Mat inv() {
		return mat.inv();
	}

	public boolean isContinuous() {
		return mat.isContinuous();
	}

	public boolean isSubmatrix() {
		return mat.isSubmatrix();
	}

	public Mat mul(Mat m, double scale) {
		return mat.mul(m, scale);
	}

	public Mat mul(Mat m) {
		return mat.mul(m);
	}

	public void push_back(Mat m) {
		mat.push_back(m);
	}

	public void release() {
		mat.release();
	}

	public Mat reshape(int cn, int rows) {
		return mat.reshape(cn, rows);
	}

	public Mat reshape(int cn) {
		return mat.reshape(cn);
	}

	public Mat row(int y) {
		return mat.row(y);
	}

	public Mat rowRange(int startrow, int endrow) {
		return mat.rowRange(startrow, endrow);
	}

	public Mat rowRange(Range r) {
		return mat.rowRange(r);
	}

	public int rows() {
		return mat.rows();
	}

	public Mat setTo(Scalar s) {
		return mat.setTo(s);
	}

	public Mat setTo(Scalar value, Mat mask) {
		return mat.setTo(value, mask);
	}

	public Size size() {
		return mat.size();
	}

	public long step1(int i) {
		return mat.step1(i);
	}

	public long step1() {
		return mat.step1();
	}

	public Mat submat(int rowStart, int rowEnd, int colStart, int colEnd) {
		return mat.submat(rowStart, rowEnd, colStart, colEnd);
	}

	public Mat submat(Range rowRange, Range colRange) {
		return mat.submat(rowRange, colRange);
	}

	public Mat submat(Rect roi) {
		return mat.submat(roi);
	}

	public Mat t() {
		return mat.t();
	}

	public long total() {
		return mat.total();
	}

	public int type() {
		return mat.type();
	}

	public String toString() {
		return mat.toString();
	}

	public String dump() {
		return mat.dump();
	}

	public int put(int row, int col, double... data) {
		return mat.put(row, col, data);
	}

	public int put(int row, int col, float[] data) {
		return mat.put(row, col, data);
	}

	public int put(int row, int col, int[] data) {
		return mat.put(row, col, data);
	}

	public int put(int row, int col, short[] data) {
		return mat.put(row, col, data);
	}

	public int put(int row, int col, byte[] data) {
		return mat.put(row, col, data);
	}

	public int get(int row, int col, byte[] data) {
		return mat.get(row, col, data);
	}

	public int get(int row, int col, short[] data) {
		return mat.get(row, col, data);
	}

	public int get(int row, int col, int[] data) {
		return mat.get(row, col, data);
	}

	public int get(int row, int col, float[] data) {
		return mat.get(row, col, data);
	}

	public int get(int row, int col, double[] data) {
		return mat.get(row, col, data);
	}

	public double[] get(int row, int col) {
		return mat.get(row, col);
	}

	public int height() {
		return mat.height();
	}

	public int width() {
		return mat.width();
	}

}
