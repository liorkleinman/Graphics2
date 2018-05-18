package edu.cg.scene.lightSources;

import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ray;
import edu.cg.scene.objects.Surface;

public class PointLight extends Light {
	protected Point position;
	
	//Decay factors:
	protected double kq = 0.01;
	protected double kl = 0.1;
	protected double kc = 1;
	
	protected String description() {
		String endl = System.lineSeparator();
		return "Intensity: " + intensity + endl +
				"Position: " + position + endl +
				"Decay factors: kq = " + kq + ", kl = " + kl + ", kc = " + kc + endl;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Point Light:" + endl + description();
	}
	
	@Override
	public PointLight initIntensity(Vec intensity) {
		return (PointLight)super.initIntensity(intensity);
	}
	
	public PointLight initPosition(Point position) {
		this.position = position;
		return this;
	}
	
	public PointLight initDecayFactors(double kq, double kl, double kc) {
		this.kq = kq;
		this.kl = kl;
		this.kc = kc;
		return this;
	}

	public boolean isBlockedBySurface(Surface surface, Ray ray){
		Hit hit = surface.intersect(ray);
		if (hit == null) {
			return false;
		} 
		Point source = ray.source();	
		Point hittingPoint = ray.getHittingPoint(hit);
		return source.distSqr(this.position) - source.distSqr(hittingPoint) > 0;
	}
	
	
	protected double getDecay(Point p) {
		double d = position.dist(p);
		double decay = kc + (kl * d) + (kq * d * d);
		// TODO: check with 1.0
		return ((double) 1 / decay);
	}

	@Override
	public Vec intersactionToLight(Point p){
		return p.sub(position).normalize();
	}

	@Override
	public Vec getIntensity(Point intersactionPoint) {
		return intensity.mult(getDecay(intersactionPoint));
	}
}
