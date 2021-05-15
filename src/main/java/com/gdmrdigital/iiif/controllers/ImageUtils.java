package com.gdmrdigital.iiif.controllers;

import javax.imageio.ImageIO;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.annotation.PostConstruct;

@RequestScoped
@ManagedBean
public class ImageUtils {
    public ImageUtils() {
        System.out.println("Constructor:");
        System.out.println(getSupportedTypes());
    }

    @PostConstruct
    public void init() {
        System.out.println("init:");
        System.out.println(getSupportedTypes());
    }

    public String getSupportedTypes() {
        String[] tMimetypes = ImageIO.getWriterMIMETypes();
        System.out.println("Types:");
        System.out.println(tMimetypes);

        return String.join(" ", tMimetypes);
    }
}
