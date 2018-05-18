package edu.cg.scene.lightSources;

import edu.cg.algebra.Vec;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.scene.objects.Surface;

public class DirectionalLight extends Light {
	private Vec direction = new Vec(0, -1, -1);

	public DirectionalLight initDirection(Vec direction) {
		this.direction = direction;
		return this;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Directional Light:" + endl + super.toString() +
				"Direction: " + direction + endl;
	}

	@Override
	public DirectionalLight initIntensity(Vec intensity) {
		return (DirectionalLight)super.initIntensity(intensity);
	}

	public boolean isBlockedBySurface(Surface surface, Ray ray){
			return surface.intersect(ray) != null;
	}

	@Override
	public Vec getIntensity(Point intersactionPoint) {
		return this.intensity;
	}

	public Vec intersactionToLight(Point p){
		return this.direction.normalize();
	}

	
	//TODO: add some methods
}
