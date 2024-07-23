package dev.chopikus.raytracer.geom;

import java.util.Optional;

import dev.chopikus.raytracer.hit.*;
import dev.chopikus.raytracer.util.*;

public class Camera {
    private int imageWidth;
    private int imageHeight;
    private Point center;
    private Point p00;
    private Vec3 dh;
    private Vec3 dv;

    public Camera(double aspectRatio, int imageWidth) {
        this.imageWidth = imageWidth;
        imageHeight = (int) (imageWidth / aspectRatio);

        /* Geometric units */
        var focalLength = 1.0;
        var viewportHeight = 2.0;
        var viewportWidth = viewportHeight * ((double) imageWidth / imageHeight);

        center = new Point(0.0, 0.0, 0.0);

        Vec3 h = new Vec3(viewportWidth, 0.0, 0.0);
        Vec3 v = new Vec3(0.0, -viewportHeight, 0.0);
        dh = h.divide(imageWidth);
        dv = v.divide(imageHeight);

        Point viewportCenter = center
                                .subtract(new Vec3(0, 0, focalLength))
                                .toPoint();

        Point viewportStart = viewportCenter
                                .subtract(v.divide(2))
                                .subtract(h.divide(2))
                                .toPoint();
        
        p00 = viewportStart
                    .add(dv.divide(2))
                    .add(dh.divide(2))
                    .toPoint();
    }

    public Color rayColor(Ray r, Hittable world) {
        Optional<HitRecord> hr = world.hit(r, new Interval(0.0, Double.POSITIVE_INFINITY));
        
        if (hr.isPresent()) {
            var normal = hr.get().normal();
            return new Color(normal.x + 1.0, normal.y + 1.0, normal.z + 1.0)
                       .divide(2.0);
        }

        Vec3 unitDir = r.direction().unit();
        /* unitDir.y() can be from -1 to 1. */
        var a = (unitDir.y + 1.0) / 2.0;
        var startColor = new Color(1.0, 1.0, 1.0);
        var endColor = new Color(0.5, 0.7, 1.0);

        return startColor
               .multiply(1.0 - a)
               .add(endColor.multiply(a));
    }

    public void render(Hittable world) {
        var image = new Image(imageWidth, imageHeight);
        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                Vec3 rayDirection = p00
                                    .add(dh.multiply(x))
                                    .add(dv.multiply(y))
                                    .subtract(center);
                
                /* There was a bug in the past creating a new Ray(pixelCenter, rayDirection) */
                Ray r = new Ray(center, rayDirection);
                image.setPixel(x, y, rayColor(r, world));
            }
        }
        image.write("target/output.png", "png");
    }
}