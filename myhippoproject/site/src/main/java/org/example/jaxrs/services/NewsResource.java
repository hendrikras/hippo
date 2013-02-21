/*
 *  Copyright 2010 Hippo.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.example.jaxrs.services;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryManager;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.jaxrs.services.AbstractResource;
import org.hippoecm.hst.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.example.beans.NewsDocument;
import org.example.jaxrs.model.DocumentRepresentation;

/**
 * @version $Id$
 */
@Path("/news/")
public class NewsResource extends AbstractResource {
    
    private static Logger log = LoggerFactory.getLogger(NewsResource.class);
    @PUT
    @Path("/{item}/{description}")
    public void addNews(@PathParam("item") String item, @QueryParam("description") String desc) {
    	//this works to
    	System.out.println("item: "+item+", "+desc);
    }
 
    @GET
    @Path("/")
    public List<DocumentRepresentation> getNewsResources(@Context HttpServletRequest servletRequest, @Context HttpServletResponse servletResponse, @Context UriInfo uriInfo,
            @QueryParam("sortby") @DefaultValue("hippogogreen:rating") String sortBy, 
            @QueryParam("sortdir") @DefaultValue("descending") String sortDirection,
            @QueryParam("max") @DefaultValue("10") String maxParam) {
        
        List<DocumentRepresentation> newsList = new ArrayList<DocumentRepresentation>();
        HstRequestContext requestContext = getRequestContext(servletRequest);
        
        try {
            String mountContentPath = requestContext.getResolvedMount().getMount().getContentPath();
            Node mountContentNode = requestContext.getSession().getRootNode().getNode(PathUtils.normalizePath(mountContentPath));
            
            HstQueryManager manager = getHstQueryManager(requestContext.getSession(), requestContext);
            HstQuery hstQuery = manager.createQuery(mountContentNode, NewsDocument.class, true);
  
            HstQueryResult result = hstQuery.execute();
            HippoBeanIterator iterator = result.getHippoBeans();

            while (iterator.hasNext()) {
                NewsDocument newsBean = (NewsDocument) iterator.nextHippoBean();
                
                if (newsBean != null) {
                    DocumentRepresentation productRep = new DocumentRepresentation(requestContext, null).represent(newsBean);
                    productRep.addLink(getNodeLink(requestContext, newsBean));
                    productRep.addLink(getSiteLink(requestContext, newsBean));
                    newsList.add(productRep);
                }
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Failed to retrieve news items.", e);
            } else {
                log.warn("Failed to retrieve newsitems. {}", e.toString());
            }
            
            throw new WebApplicationException(e);
        }
        return newsList;
    }

}
