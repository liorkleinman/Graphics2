package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Triangle extends Shape {
	private Point p1, p2, p3;
	private transient Plain plain;

	public Triangle() {
		p1 = p2 = p3 = null;
	}

	public Triangle(Point p1, Point p2, Point p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.initTrianglePlain();
	}

	private void initTrianglePlain() {
		Vec v2 = this.p2.sub(this.p1);
		Vec v3 = this.p3.sub(this.p1);
		Vec nomal = v2.cross(v3).normalize();
		this.plain = new Plain();
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Triangle:" + endl + "p1: " + p1 + endl + "p2: " + p2 + endl + "p3: " + p3 + endl;
	}

	private Hit verifyHitInside(Ray ray, Hit planeHit) {
		if (planeHit == null) {
			return null;
		}

		Point pointHit = ray.getHittingPoint(planeHit);
		Ray sourceToHitPointRay = new Ray(ray.source(), pointHit);
		Vec N1 = getTriangleProjectionNorm(this.p1, this.p2, ray.source());
		Vec N2 = getTriangleProjectionNorm(this.p2, this.p3, ray.source());
		Vec N3 = getTriangleProjectionNorm(this.p3, this.p1, ray.source());
		
		// TODO: check greater then or greater
		boolean sign1 = N1.dot(sourceToHitPointRay.direction()) >= 0;
		boolean sign2 = N2.dot(sourceToHitPointRay.direction()) >= 0;
		boolean sign3 = N3.dot(sourceToHitPointRay.direction()) >= 0;

		return this.signsEqual(sign1, sign2, sign3) ? planeHit : null;
	}

	private boolean signsEqual(boolean sign1, boolean sign2, boolean sign3) {
		return ((sign1 && sign2 && sign3) || (!sign1 && !sign2 && !sign3));
	}

	private Vec getTriangleProjectionNorm(Point t1, Point t2, Point p0) {
		Vec v1 = t1.sub(p0);
		Vec v2 = t2.sub(p0);
		return v1.cross(v2).mult(1 / v1.cross(v2).norm()) ;
	}

	@Override
	public Hit intersect(Ray ray) {
        Hit planeHit = this.plain.intersect(ray);
        return this.verifyHitInside(ray, planeHit);
	}
}
