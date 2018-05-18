package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cg.Logger;
import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Surface;

public class Scene {
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; //gets the values of 1, 2 and 3
	private boolean renderRefarctions = false;
	private boolean renderReflections = false;
	
	private Point camera = new Point(0, 0, 5);
	private Vec ambient = new Vec(1, 1, 1); //white
	private Vec backgroundColor = new Vec(0, 0.5, 1); //blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();
	
	
	//MARK: initializers
	public Scene initCamera(Point camera) {
		this.camera = camera;
		return this;
	}
	
	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}
	
	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}
	
	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}
	
	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}
	
	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}
	
	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}
	
	public Scene initName(String name) {
		this.name = name;
		return this;
	}
	
	public Scene initRenderRefarctions(boolean renderRefarctions) {
		this.renderRefarctions = renderRefarctions;
		return this;
	}
	
	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}
	
	//MARK: getters
	public String getName() {
		return name;
	}
	
	public int getFactor() {
		return antiAliasingFactor;
	}
	
	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}
	
	public boolean getRenderRefarctions() {
		return renderRefarctions;
	}
	
	public boolean getRenderReflections() {
		return renderReflections;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator(); 
		return "Camera: " + camera + endl +
				"Ambient: " + ambient + endl +
				"Background Color: " + backgroundColor + endl +
				"Max recursion level: " + maxRecursionLevel + endl +
				"Anti aliasing factor: " + antiAliasingFactor + endl +
				"Light sources:" + endl + lightSources + endl +
				"Surfaces:" + endl + surfaces;
	}
	
	private static class IndexTransformer {
		private final int max;
		private final int deltaX;
		private final int deltaY;
		
		IndexTransformer(int width, int height) {
			max = Math.max(width, height);
			deltaX = (max - width) / 2;
			deltaY = (max - height) / 2;
		}
		
		Point transform(int x, int y) {
			double xPos = (2*(x + deltaX) - max) / ((double)max);
			double yPos = (max - 2*(y + deltaY)) / ((double)max);
			return new Point(xPos, yPos, 0);
		}
	}
	
	private transient IndexTransformer transformaer = null;
	private transient ExecutorService executor = null;
	private transient Logger logger = null;
	
	private void initSomeFields(int imgWidth, int imgHeight, Logger logger) {
		this.logger = logger;
		//TODO: initialize your additional field here.
	}
	
	
	public BufferedImage render(int imgWidth, int imgHeight, Logger logger)
			throws InterruptedException, ExecutionException {
		
		initSomeFields(imgWidth, imgHeight, logger);
		
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		transformaer = new IndexTransformer(imgWidth, imgHeight);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Intitialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);
		
		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][])(new Future[imgHeight][imgWidth]);
		
		this.logger.log("Starting to shoot " +
			(imgHeight*imgWidth*antiAliasingFactor*antiAliasingFactor) +
			" rays over " + name);
		
		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x)
				futures[y][x] = calcColor(x, y);
		
		this.logger.log("Done shooting rays.");
		this.logger.log("Wating for results...");
		
		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}
		
		executor.shutdown();
		
		this.logger.log("Ray tracing of " + name + " has been completed.");
		
		executor = null;
		transformaer = null;
		this.logger = null;
		
		return img;
	}
	
	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			//TODO: change this method implementation to implement super sampling
			Point pointOnScreenPlain = transformaer.transform(x, y);
			Ray ray = new Ray(camera, pointOnScreenPlain);
			return calcColor(ray, 0).toColor();
		});
	}
	
	private Vec calcColor(Ray ray, int recusionLevel) {
		Hit closestHit = intersaction(ray);
		if(closestHit == null){
			return this.backgroundColor;
		}
		Surface surface = closestHit.getSurface();
		Vec color = surface.Ka().mult(this.ambient);

		color.add(getLightImpect(closestHit, ray));

		if (this.renderReflections) {
			color.add(getReflectionsColor(closestHit, ray, recusionLevel));
		}

		if (this.renderRefarctions) {
			color.add(getRefarctionsColor(closestHit, ray, recusionLevel));
		}
		return color;
	}

	
	private Vec getLightImpect(Hit closestHit, Ray ray){
		Vec TotalLightImpact = new Vec();
		for (Light light : lightSources){
			if(!light.isBlocked(surfaces, ray, closestHit)){
				Vec color = getDeffuseEffect(closestHit, ray);
				color = color.add(getSpecularEffect(closestHit, ray));
				TotalLightImpact = TotalLightImpact.add(color.mult(light.getIntensity(closestHit, ray)));
			}


		}
	}

	private Hit intersaction(Ray ray){
		Hit minHit = null;
		double minDistance = Double.MAX_VALUE;
		for (Surface surface : surfaces) {
			Hit currentSurfaceHit = surface.intersect(ray);
			if (currentSurfaceHit != null && currentSurfaceHit.t() <= minDistance){
				minDistance = currentSurfaceHit.t();
				minHit = currentSurfaceHit;
				//TODO set surface?
			} 	
		}
			return minHit;
	}

	private Vec getReflectionsColor(Hit closestHit, Ray ray, int recusionLevel) {
		Surface surface = closestHit.getSurface();
		Point hittingPoint = ray.getHittingPoint(closestHit);
		Vec refrectionColor = Ops.reflect(ray.direction(), closestHit.getNormalToSurface());
		Vec Weight = surface.Ks().mult(surface.reflectionIntensity());
		return  calcColor(new Ray(hittingPoint, refrectionColor),
			recusionLevel + 1).mult(Weight);
	}

	private Vec getRefarctionsColor(Hit closestHit, Ray ray, int recusionLevel) {
		Surface surface = closestHit.getSurface();
		Point hittingPoint = ray.getHittingPoint(closestHit);
		double WithInSurface1 = surface.n1(closestHit);
		double WithInSurface2 = surface.n2(closestHit);
		Vec direction = Ops.refract(ray.direction(), closestHit.getNormalToSurface(), 
			WithInSurface1, WithInSurface2);
		Vec Weight = surface.Kt().mult(surface.refractionIntensity());
		return  calcColor(new Ray(hittingPoint, direction),
				 recusionLevel + 1).mult(Weight);
	}

}
