package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Dome extends Shape {
	private Sphere sphere;
	private Plain plain;
	
	public Dome() {
		sphere = new Sphere().initCenter(new Point(0, -0.5, -6));
		plain = new Plain(new Vec(-1, 0, -1), new Point(0, -0.5, -6));
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Dome:" + endl + 
				sphere + plain + endl;
	}
	
	@Override
	public Hit intersect(Ray ray) {
		Hit hitSphere = sphere.intersect(ray);
		if (hitSphere == null) {
		    return null;
        }

        Point hitPointS = ray.source().add(hitSphere.t(), ray.direction());
		if (plain.normal().dot(hitPointS.toVec()) >= 0) {
		    return hitSphere;
        }
		// TODO: check if irelevent
        Hit plainHit = plain.intersect(ray);
		Point hitPointP = ray.source().add(plainHit.t(), ray.direction());
        if(hitPointP.dist(sphere.getCenter()) - sphere.getRadius() <  0) {
            return plainHit;
        }

        return null;
	}
}
