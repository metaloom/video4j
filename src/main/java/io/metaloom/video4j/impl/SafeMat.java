package io.metaloom.video4j.impl;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

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

	public Mat adjustROI(int dtop, int dbottom, int dleft, int dright) {
		return mat.adjustROI(dtop, dbottom, dleft, dright);
	}

	public void assignTo(Mat m, int type) {
		mat.assignTo(m, type);
	}

	public void assignTo(Mat m) {
		mat.assignTo(m);
	}

	public int channels() {
		return mat.channels();
	}

	public int checkVector(int elemChannels, int depth, boolean requireContinuous) {
		return mat.checkVector(elemChannels, depth, requireContinuous);
	}

	public int checkVector(int elemChannels, int depth) {
		return mat.checkVector(elemChannels, depth);
	}

	public boolean equals(Object obj) {
		return mat.equals(obj);
	}

	public int checkVector(int elemChannels) {
		return mat.checkVector(elemChannels);
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

	public void convertTo(Mat m, int rtype, double alpha) {
		mat.convertTo(m, rtype, alpha);
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

	public void create(int rows, int cols, int type) {
		mat.create(rows, cols, type);
	}

	public void create(Size size, int type) {
		mat.create(size, type);
	}

	public void create(int[] sizes, int type) {
		mat.create(sizes, type);
	}

	public void copySize(Mat m) {
		mat.copySize(m);
	}

	public Mat cross(Mat m) {
		return mat.cross(m);
	}

	public long dataAddr() {
		return mat.dataAddr();
	}

	public int depth() {
		return mat.depth();
	}

	public Mat diag(int d) {
		return mat.diag(d);
	}

	public Mat diag() {
		return mat.diag();
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

	public void locateROI(Size wholeSize, Point ofs) {
		mat.locateROI(wholeSize, ofs);
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

	public Mat reshape(int cn, int[] newshape) {
		return mat.reshape(cn, newshape);
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

	public Mat setTo(Mat value, Mat mask) {
		return mat.setTo(value, mask);
	}

	public Mat setTo(Mat value) {
		return mat.setTo(value);
	}

	public Size size() {
		return mat.size();
	}

	public int size(int i) {
		return mat.size(i);
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

	public Mat submat(Range[] ranges) {
		return mat.submat(ranges);
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

	public int put(int[] idx, double... data) {
		return mat.put(idx, data);
	}

	public int put(int row, int col, float[] data) {
		return mat.put(row, col, data);
	}

	public int put(int[] idx, float[] data) {
		return mat.put(idx, data);
	}

	public int put(int row, int col, int[] data) {
		return mat.put(row, col, data);
	}

	public int put(int[] idx, int[] data) {
		return mat.put(idx, data);
	}

	public int put(int row, int col, short[] data) {
		return mat.put(row, col, data);
	}

	public int put(int[] idx, short[] data) {
		return mat.put(idx, data);
	}

	public int put(int row, int col, byte[] data) {
		return mat.put(row, col, data);
	}

	public int put(int[] idx, byte[] data) {
		return mat.put(idx, data);
	}

	public int put(int row, int col, byte[] data, int offset, int length) {
		return mat.put(row, col, data, offset, length);
	}

	public int put(int[] idx, byte[] data, int offset, int length) {
		return mat.put(idx, data, offset, length);
	}

	public int get(int row, int col, byte[] data) {
		return mat.get(row, col, data);
	}

	public int get(int[] idx, byte[] data) {
		return mat.get(idx, data);
	}

	public int get(int row, int col, short[] data) {
		return mat.get(row, col, data);
	}

	public int get(int[] idx, short[] data) {
		return mat.get(idx, data);
	}

	public int get(int row, int col, int[] data) {
		return mat.get(row, col, data);
	}

	public int get(int[] idx, int[] data) {
		return mat.get(idx, data);
	}

	public int get(int row, int col, float[] data) {
		return mat.get(row, col, data);
	}

	public int get(int[] idx, float[] data) {
		return mat.get(idx, data);
	}

	public int get(int row, int col, double[] data) {
		return mat.get(row, col, data);
	}

	public int get(int[] idx, double[] data) {
		return mat.get(idx, data);
	}

	public double[] get(int row, int col) {
		return mat.get(row, col);
	}

	public double[] get(int[] idx) {
		return mat.get(idx);
	}

	public int height() {
		return mat.height();
	}

	public int width() {
		return mat.width();
	}

	public long getNativeObjAddr() {
		return mat.getNativeObjAddr();
	}

}
