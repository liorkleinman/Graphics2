package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Sphere extends Shape {
	private Point center;
	private double radius;
	
	public Sphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}
	
	public Sphere() {
		this(new Point(0, -0.5, -6), 0.5);
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Sphere:" + endl + 
				"Center: " + center + endl +
				"Radius: " + radius + endl;
	}
	
	public Sphere initCenter(Point center) {
		this.center = center;
		return this;
	}
	
	public Sphere initRadius(double radius) {
		this.radius = radius;
		return this;
	}
	
	@Override
	public Hit intersect(Ray ray) {
		Point p0 = ray.source();
		Vec v = ray.direction();
		double aCoeff = 1;
		double bCoeff = v.mult(2).dot(p0.sub(this.center));
		double cCoeff =	p0.distSqr(this.center) - (this.radius * this.radius);
		double delta = Math.sqrt((bCoeff * bCoeff) - 4 * aCoeff * cCoeff);
		
		// if delta < 0 there is not hit
		if (Double.isNaN(delta)){
			return null;
		} 

		double largerX = ((-1 * bCoeff) + delta) / 2.0;
		double smallerX = ((-1 * bCoeff) - delta) / 2.0;

		double t = smallerX < 0 ? largerX : smallerX;
		//  if the larger solution of the equation is negative, there is not hit
		if (largerX < 0 || Double.isNaN(t)){
			return null;
		} 
		Vec normal = ray.add(t).sub(this.center).normalize() ;
		//TODO check if neg() in case of choosing the largerX
		return new Hit (t, normal);
	}
}