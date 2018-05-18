package edu.cg.scene.lightSources;

import java.util.List;

import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Surface;

import edu.cg.algebra.Vec;

public abstract class Light {
	protected Vec intensity = new Vec(1, 1, 1); //white color
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Intensity: " + intensity + endl;
	}
	
	public Light initIntensity(Vec intensity) {
		this.intensity = intensity;
		return this;
	}

	public boolean isBlocked(List<Surface> surfaces, Ray ray, Hit hitPoint){
		for (Surface surface : surfaces) {
			if(this.isBlockedBySurface(surface, ray)){
				return true;
			}
		}
		return false;
	}

	public abstract Vec intersactionToLight(Point p);

	public abstract Vec getIntensity(Point intersactionPoint);
	
	public abstract boolean isBlockedBySurface(Surface surface, Ray ray);
	
	//TODO: add some methods
}
