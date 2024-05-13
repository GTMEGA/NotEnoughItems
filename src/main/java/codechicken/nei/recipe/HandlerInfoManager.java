package codechicken.nei.recipe;


import codechicken.nei.NEIClientConfig;

import cpw.mods.fml.common.Loader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;


public final class HandlerInfoManager {
    public static HandlerInfo DEFAULT_HANDLER_INFO = getDefaultHandlerInfo();
    public static HashMap<String, HandlerInfo> handlerMap = new HashMap<>();

    private HandlerInfoManager() {}

    public static HandlerInfo getHandlerInfo(String name, String name2) {
        HandlerInfo res = handlerMap.get(name);
        if (res == null) res = handlerMap.get(name2);
        
        return res;
    }

    public static void loadHandlerInfo() {
        final boolean fromJar = NEIClientConfig.loadHandlersFromJar();
        NEIClientConfig.logger.info("Loading handler info from " + (fromJar ? "JAR" : "Config"));
        handlerMap.clear();
        URL handlerUrl = Thread.currentThread().getContextClassLoader().getResource("assets/nei/csv/handlers.csv");
        
        URL url;
        if (fromJar) {
            url = handlerUrl;
            if (url == null) {
                NEIClientConfig.logger.info("Invalid URL for handlers csv.");
                return;
            }
        } else {
            File handlerFile = NEIClientConfig.handlerFile;
            if(!handlerFile.exists()) {
                NEIClientConfig.logger.info("Config file doesn't exit, creating");
                try {
                    assert handlerUrl != null;
                    ReadableByteChannel readableByteChannel = Channels.newChannel(handlerUrl.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(handlerFile.getAbsoluteFile());
                    FileChannel fileChannel = fileOutputStream.getChannel();
                    fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            try {
                url = NEIClientConfig.handlerFile.toURI().toURL();
            } catch (MalformedURLException e) {
                NEIClientConfig.logger.info("Invalid URL for handlers csv (via config).");
                e.printStackTrace();
                return;
            }
        }
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()));
            CSVParser csvParser = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(input);
            for (CSVRecord record : csvParser) {
                final String handler = record.get("handler");
                final String modName = record.get("modName");
                final String modId = record.get("modId");
                final boolean requiresMod = Boolean.parseBoolean(record.get("modRequired"));
                final String excludedModId = record.get("excludedModId");

                if (requiresMod && !Loader.isModLoaded(modId)) continue;
                if (excludedModId != null && Loader.isModLoaded(excludedModId)) continue;
                
                
                HandlerInfo info = new HandlerInfo(handler, modName, modId, requiresMod, excludedModId);
                final String imageResource = record.get("imageResource");
                if(imageResource != null && !imageResource.equals("")) {
                    info.setImage(imageResource, 
                        Integer.parseInt(record.get("imageX")), 
                        Integer.parseInt(record.get("imageY")),
                        Integer.parseInt(record.get("imageWidth")),
                        Integer.parseInt(record.get("imageHeight"))
                    );
                }
                if(!info.hasImageOrItem()) {
                    final String itemName = record.get("itemName");
                    if (itemName != null && !itemName.equals("")) {
                        info.setItem(itemName, record.get("nbtInfo"));
                    }
                }
                final String yShift = record.get("yShift");
                if (yShift != null && !yShift.equals(""))
                    info.setYShift(Integer.parseInt(yShift));
                
                try {
                    final int imageHeight = intOrDefault(record.get("handlerHeight"), HandlerInfo.DEFAULT_HEIGHT);
                    final int imageWidth = intOrDefault(record.get("handlerWidth"), HandlerInfo.DEFAULT_WIDTH);
                    final int maxRecipesPerPage = intOrDefault(record.get("maxRecipesPerPage"), HandlerInfo.DEFAULT_MAX_PER_PAGE);
                    info.setHandlerDimensions(imageHeight, imageWidth, maxRecipesPerPage);
                } catch (NumberFormatException ignored) {
                    NEIClientConfig.logger.info("Error setting handler dimensions for " + handler);
                }
                
                handlerMap.put(handler, info);
                NEIClientConfig.logger.info("Loaded " + handler);
            }
        } catch (IOException e) {
            NEIClientConfig.logger.info("Error parsing CSV");
            e.printStackTrace();
        } catch (Exception e) {
            NEIClientConfig.logger.info("Error parsing CSV");
            e.printStackTrace();
            
        }

    }
    
    private static HandlerInfo getDefaultHandlerInfo() {
        final HandlerInfo info = new HandlerInfo("Unknown", "Unknown", "Unknown", false, "");
        info.setHandlerDimensions(HandlerInfo.DEFAULT_HEIGHT, HandlerInfo.DEFAULT_WIDTH, HandlerInfo.DEFAULT_MAX_PER_PAGE);
        return info;
    }

    private static int intOrDefault(String str, int defaultValue) {
        if (str == null || str.equals("")) return defaultValue;
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


}
