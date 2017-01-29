package numberPlate.configurator;

import java.util.*;
import java.io.*;
import java.awt.*;

public class Configurator {
    /* Default name of configuration file */
    private String fileName = new String("config.xml");
    /* Configuration file's comment */
    private String comment = new String("This si global configuration file for Automatic Number Plate Recognition System");
    
    /* Primary property list containing values from configuration file */
    private Properties list;
    
    public Configurator() {
        list = new Properties();
        /* ***** BEGIN *** Definition of property defaults  ******* */
        
        // PHOTO
        
        // adaptive thresholding radius (0 = no adaptive)
        this.setIntProperty("photo_adaptivethresholdingradius", 7); // 7 is recommanded
        
        // Bindgraph - processing a horizontal projection of the detected field marks
        //the X axis is detected peak, peakfoot, and finally multiply it by a constant p.d.m.c
        this.setDoubleProperty("bandgraph_peakfootconstant", 0.55); //0.75
        this.setDoubleProperty("bandgraph_peakDiffMultiplicationConstant", 0.2);
        
        // CARSNAPSHOT
        this.setIntProperty("carsnapshot_distributormargins", 25);
        this.setIntProperty("carsnapshot_graphrankfilter", 9);
        
        
        // CARSNAPSHOTGRAPH
        this.setDoubleProperty("carsnapshotgraph_peakfootconstant", 0.55); //0.55
        this.setDoubleProperty("carsnapshotgraph_peakDiffMultiplicationConstant", 0.1);
        
        
        this.setIntProperty("intelligence_skewdetection", 0);
        
        // CHAR
        // this.setDoubleProperty("char_contrastnormalizationconstant", 0.5);  //1.0
        this.setIntProperty("char_normalizeddimensions_x", 8); //8
        this.setIntProperty("char_normalizeddimensions_y", 13); //13
        this.setIntProperty("char_resizeMethod",1); // 0=linear 1=average
        this.setIntProperty("char_featuresExtractionMethod",0); //0=map, 1=edge
        this.setStrProperty("char_neuralNetworkPath","./resources/neuralnetworks/network_avgres_813_map.xml");
        this.setStrProperty("char_learnAlphabetPath","./resources/alphabets/alphabet_8x13");
        this.setIntProperty("intelligence_classification_method",0); // 0 = pattern match ,1=nn
     
        // PLATEGRAPH
        this.setDoubleProperty("plategraph_peakfootconstant", 0.7); // Specifies the width of the gap detected
        this.setDoubleProperty("plategraph_rel_minpeaksize", 0.86); // 0.85 // a smaller number Seka characters, bigger again incorrectly associates
        
        // PLATEGRAPHHORIZONTALGRAPH
        this.setDoubleProperty("platehorizontalgraph_peakfootconstant", 0.05);
        this.setIntProperty("platehorizontalgraph_detectionType",1); // 1=edgedetection 0=magnitudederivate
        
        // PLATEVERICALGRAPH
        this.setDoubleProperty("plateverticalgraph_peakfootconstant", 0.42);
        
        // INTELLIGENCE
        this.setIntProperty("intelligence_numberOfBands",3);
        this.setIntProperty("intelligence_numberOfPlates",3);
        this.setIntProperty("intelligence_numberOfChars",20);
        
        this.setIntProperty("intelligence_minimumChars",5);
        this.setIntProperty("intelligence_maximumChars",15);
        
        // plate heuristics
        this.setDoubleProperty("intelligence_maxCharWidthDispersion",0.5); // in plate
        this.setDoubleProperty("intelligence_minPlateWidthHeightRatio",0.5);
        this.setDoubleProperty("intelligence_maxPlateWidthHeightRatio",15.0);
        
        // char heuristics
        this.setDoubleProperty("intelligence_minCharWidthHeightRatio",0.1);
        this.setDoubleProperty("intelligence_maxCharWidthHeightRatio",0.92);
        this.setDoubleProperty("intelligence_maxBrightnessCostDispersion", 0.161);
        this.setDoubleProperty("intelligence_maxContrastCostDispersion", 0.1);
        this.setDoubleProperty("intelligence_maxHueCostDispersion", 0.145);
        this.setDoubleProperty("intelligence_maxSaturationCostDispersion", 0.24); //0.15
        this.setDoubleProperty("intelligence_maxHeightCostDispersion", 0.2);
        this.setDoubleProperty("intelligence_maxSimilarityCostDispersion", 100);
        
        // RECOGNITION
        this.setIntProperty("intelligence_syntaxanalysis",2);
        this.setStrProperty("intelligence_syntaxDescriptionFile","./resources/syntax/syntax.xml");
        
        // NEURAL NETWORK
        //int maxK, double eps, double lambda, double micro
        
        this.setIntProperty("neural_maxk", 8000); // maximum K - maximum number of iterations
        this.setDoubleProperty("neural_eps", 0.07); // epsilon - Required accuracy
        this.setDoubleProperty("neural_lambda", 0.05); // lambda factor - learning rate, gradient magnitude
        this.setDoubleProperty("neural_micro", 0.5); // micro -Torque members for overcoming local extremes
        // top(log(m recognized units)) = 6
        this.setIntProperty("neural_topology", 20); // topologia middle class
        
        /* ***** END ***** Definition of property defaults  ******* */
        
        this.setStrProperty("help_file_help","./resources/help/help.html");
        this.setStrProperty("help_file_about","./resources/help/about.html");
        this.setStrProperty("reportgeneratorcss","./resources/reportgenerator/style.css");
    }
    public Configurator(String path) {
        this();
        try {
            loadConfiguration(path);
        } catch (Exception ex) {
            System.out.println("Error: Couldn't load configuration file "+path);
            System.exit(1);
        }
    }
    
    public void setConfigurationFileName(String name) {
        this.fileName = name;
    }
    
    public String getConfigurationFileName() {
        return this.fileName;
    }
    
    public String getStrProperty(String name) {
        return list.getProperty(name).toString();
    }
    
    public String getPathProperty(String name) {
        return this.getStrProperty(name).replace('/',File.separatorChar);
        
    }
    
    public void setStrProperty(String name, String value) {
        list.setProperty(name, value);
    }
    
    public int getIntProperty(String name) throws NumberFormatException {
        return Integer.decode(list.getProperty(name));
    }
    
    public void setIntProperty(String name, int value) {
        list.setProperty(name, String.valueOf(value));
    }
    
    public double getDoubleProperty(String name) throws NumberFormatException {
        return Double.parseDouble(list.getProperty(name));
    }
    
    public void setDoubleProperty(String name, double value) {
        list.setProperty(name, String.valueOf(value));
    }
    
    public Color getColorProperty(String name) {
        return new Color(Integer.decode(list.getProperty(name)));
    }
    
    public void setColorProperty(String name, Color value) {
        list.setProperty(name, String.valueOf(value.getRGB()));
    }
    
    public void saveConfiguration() throws IOException {
        FileOutputStream os = new FileOutputStream(fileName);
        list.storeToXML(os, comment);
    }
    
    public void saveConfiguration(String arg_file) throws IOException {
        FileOutputStream os = new FileOutputStream(arg_file);
        list.storeToXML(os, comment);
    }
    
    public void loadConfiguration() throws IOException {
        FileInputStream is = new  FileInputStream(fileName);
        list.loadFromXML(is);
    }
    
    public void loadConfiguration(String arg_file) throws IOException {
        FileInputStream is = new  FileInputStream(arg_file);
        list.loadFromXML(is);
    }
    
}

