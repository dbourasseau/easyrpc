package easyrpc;
/*
 * ----------------------------------------------------------------------------
 * This code is distributed under a Beer-Ware license
 * ----------------------------------------------------------------------------
 * Mario Macias wrote this file. Considering this, you can do what the fuck you
 * want: modify it, distribute it, sell it, etc. But you MUST always credit me
 * as the original author of this code. In addition, if we met some day and you
 * think this code was useful to you, you MUST pay me a beer (a good one, if
 * possible) as reward for my contribution.
 *
 * Mario Macias Lloret, 2014
 * ----------------------------------------------------------------------------
 */

import easyrpc.marshall.PropertiesMarshaller;
import easyrpc.server.service.RpcService;
import easyrpc.unmarshall.PropertiesUnmarshaller;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mmacias on 08/02/14.
 */
public class RpcServer {
    // Key : name of the implementing interfaces
    private Map<String,Object> endpoints = new TreeMap<String, Object>();

    protected RpcService serviceLayer;
    protected PropertiesUnmarshaller unmarshaller;

    public RpcServer(RpcService serviceLayer, PropertiesUnmarshaller unmarshaller) {
        this.serviceLayer = serviceLayer;
        this.unmarshaller = unmarshaller;

        serviceLayer.setRpcServer(this);
    }

    public Object getEndpoint(Class iface) {
        return endpoints.get(iface.getCanonicalName());
    }

    public void addEndpoint(Object o) {
        Class c = o.getClass();
        Class[] interfaces = c.getInterfaces();
        for(Class iface : interfaces) {
            if(endpoints.get(iface.getCanonicalName()) != null) {
                Logger.getLogger(RpcServer.class.getCanonicalName()).log(Level.WARNING,
                        "Registering class " + c.getCanonicalName() + ". Interface "
                                + iface.getCanonicalName() + " was already registered. Overwriting");
            }
            endpoints.put(iface.getCanonicalName(), o);
        }
    }

    public void start() {
        serviceLayer.start();
    }

    public PropertiesUnmarshaller getUnmarshaller() {
        return unmarshaller;
    }


    public byte[] forwardCall(String endpoint, byte[] data) {
        Object o = endpoints.get(endpoint);
        if(o == null) throw new RuntimeException("Endpoint " + endpoint + " does not exist");
        // todo : LLAMAR A MATCHMETHOD DE UNMARSHALLER
        // quitar todo esto de properties
        Properties p = new Properties();
        try {
            p.load(new StringReader(new String(data)));

            // mirar cuando la propiedad es un tipo primitivo: int, double, void...
            Class rt = Class.forName(p.getProperty(PropertiesMarshaller.RETURN_TYPE));
            Properties rp = new Properties();
            rp.setProperty(PropertiesMarshaller.RETURN_TYPE,rt.getCanonicalName());
            if(rt.isPrimitive()) {
                rp.setProperty(PropertiesMarshaller.RETURN_VALUE,"0");
            }
            return rp.toString().getBytes();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
