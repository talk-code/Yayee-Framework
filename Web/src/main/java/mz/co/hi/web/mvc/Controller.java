package mz.co.hi.web.mvc;

import mz.co.hi.web.FrontEnd;
import mz.co.hi.web.RequestContext;
import mz.co.hi.web.Helper;
import mz.co.hi.web.config.AppConfigurations;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mario Junior.
 */
public class Controller implements Serializable {


    public static final String VIEW_DATA_KEY ="dataJson";
    //private RequestContext requestContext;

    @Inject
    private HTMLizer htmLizer;


    public Controller(){



    }


    public JsonObjectBuilder json(){

        return Json.createObjectBuilder();

    }

    /*
    public void setRequestContext(RequestContext requestContext){

        this.requestContext = requestContext;

    }

    public RequestContext getRequestContext() {
        return requestContext;
    }*/

    public void redirect(String url){



    }


    public void callView() throws MvcException{

        this.callView(null);

    }


    public void callView(Map values) throws NoSuchViewException, TemplateException, ConversionFailedException {


        AppConfigurations config = AppConfigurations.get();
        RequestContext requestContext = CDI.current().select(RequestContext.class).get();

        String actionName = requestContext.getData().get("action").toString();
        String controllerName = requestContext.getData().get("controller").toString();
        String viewFile = "/"+config.getViewsDirectory()+"/"+controllerName+"/"+actionName.toString()+".html";
        String viewJsfile = "/"+config.getViewsDirectory()+"/"+controllerName+"/"+actionName.toString()+".js";
        String viewJsMinifiedfile = "/"+config.getViewsDirectory()+"/"+controllerName+"/"+actionName.toString()+".min.js";

        FrontEnd frontEnd = CDI.current().select(FrontEnd.class).get();

        if(values==null){

            values = new HashMap<>();

        }


        if(frontEnd.wasTemplateDataSet()){

            values.put("$root",frontEnd.getTemplateData());

        }

        requestContext.getData().put(VIEW_DATA_KEY,values);

        //Do not need to load the view file
        if(requestContext.getData().containsKey("ignore_view")){

            htmLizer.process(this,true);
            return;

        }


        URL viewResource = null;
        URL viewJsResource = null;

        try {


            viewResource = requestContext.getServletContext().getResource(viewFile);


        }catch (Exception ex){

            throw new NoSuchViewException(controllerName,actionName);

        }



        try{



            if(AppConfigurations.get().underDevelopment())

                viewJsResource   = requestContext.getServletContext().getResource(viewJsfile);

            else{

                //Try the minfied file
                viewJsResource = requestContext.getServletContext().getResource(viewJsMinifiedfile);

                if(viewJsResource==null){

                    viewJsResource   = requestContext.getServletContext().getResource(viewJsfile);

                }


            }



        }catch (Exception ex){

            //TODO: Do something about it
            ex.printStackTrace();

        }

        if(requestContext.getRequest().getHeader("Ignore-Js")==null) {

            if (viewJsResource != null) {


                try {

                    InputStream viewJsInputStream = viewJsResource.openStream();
                    String viewJsContent = Helper.readTextStreamToEnd(viewJsInputStream,null);
                    requestContext.getData().put("view_js", viewJsContent);

                } catch (Exception ex) {

                    //TODO: Do something about it
                    ex.printStackTrace();

                }

            }

        }


        if(viewResource==null){

            throw new NoSuchViewException(controllerName,actionName);

        }


        if(requestContext.getRequest().getHeader("Ignore-View")==null){

            try {

                InputStream viewInputStream = viewResource.openStream();
                String viewContent = Helper.readTextStreamToEnd(viewInputStream, null);
                requestContext.getData().put("view_content",viewContent);


            }catch (Exception ex){

                //TODO: Do something about it
                ex.printStackTrace();

            }

        }

        htmLizer.setRequestContext(requestContext);
        htmLizer.process(this,false);

    }

}
