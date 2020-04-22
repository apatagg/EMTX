/*
 * Copyright (c) 2011-2020 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.here.android.example.routing;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.TransitType;
import com.here.android.mpa.isoline.Isoline;
import com.here.android.mpa.isoline.IsolineError;
import com.here.android.mpa.isoline.IsolinePlan;
import com.here.android.mpa.isoline.IsolineRouter;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.MapLabeledMarker;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapTransitLayer;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Maneuver;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteElement;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.Router;
import com.here.android.mpa.routing.RoutingError;
import com.here.android.mpa.search.Category;
import com.here.android.mpa.search.CategoryFilter;
import com.here.android.mpa.search.DiscoveryResult;
import com.here.android.mpa.search.DiscoveryResultPage;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.ExploreRequest;
import com.here.android.mpa.search.PlaceLink;
import com.here.android.mpa.search.Request;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.SearchRequest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class MapFragmentView {
    public static List<DiscoveryResult> s_ResultList;
    public static List<PlaceLink> placesRoute = new ArrayList<>();
    private List<MapObject> m_mapObjectList = new ArrayList<>();
    private AndroidXMapFragment m_mapFragment;
    private Button m_createWaypointsButton;
    private Button m_createRouteButton;
    private Button m_createIsoline;
    private Switch switchButton;
    private ArrayList<MapLabeledMarker> m_mapRecommendedList = new ArrayList<>();
    private MapLabeledMarker mapMarkerInicio;
    private MapLabeledMarker mapMarkerFin;
    private AppCompatActivity m_activity;
    private Map m_map;
    private EditText textOrigin;
    private EditText textInterest;
    private EditText textArrival;
    private GeoCoordinate valencia;
    private PlaceLink[] puntosMapa = new PlaceLink[3];
    private List<Date> fechaIniAux = new ArrayList<>(100);
    private List<Date> fechaFinAux = new ArrayList<>(100);
    private String busIda = "";
    private String busVuelta;

    private EditText fechaInicio;
    private EditText fechaFin;
    private Date horaInicio;
    private Date horaFin;


    private AndroidXMapFragment getMapFragment() {
        return (AndroidXMapFragment) m_activity.getSupportFragmentManager().findFragmentById(R.id.mapfragment);
    }

    public MapFragmentView(AppCompatActivity activity) {
        m_activity = activity;
        initMapFragment();

        valencia = new GeoCoordinate(39.466667, -0.375000, 0.0);
        textOrigin = (EditText) m_activity.findViewById(R.id.textOrigin);
        textInterest = (EditText) m_activity.findViewById(R.id.textInterest);
        textArrival = (EditText) m_activity.findViewById(R.id.textArrival);

        fechaInicio = m_activity.findViewById(R.id.fechaInicio);
        fechaFin = m_activity.findViewById(R.id.fechaFin);

        initButtons();
    }

    private void initButtons() {
        m_createWaypointsButton = (Button) m_activity.findViewById(R.id.btnWaypoints);
        m_createIsoline = (Button) m_activity.findViewById(R.id.btnIsoline);
        m_createRouteButton = (Button) m_activity.findViewById(R.id.btnRoute);
        switchButton = (Switch) m_activity.findViewById(R.id.switchButton);

        //Boton para agregar los puntos al mapa
        m_createWaypointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createWaypoints();
                /*for (Category cat:Category.globalCategory(Category.Global.SHOPPING).getSubCategories()) {
                    System.out.println(cat.getName());
                }*/
            }
        });

        //Boton para crear la primera ruta
        m_createIsoline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createIsoline();
            }
        });

        //Boton para crear la segunda ruta
        m_createRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRoute();
            }
        });

        /*buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String test = "|" + textOrigin.getText()  + "|" + textInterest.getText() + "|" + textArrival.getText();
            }
        });*/

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cambiarLosSitiosdeInteres();
            }
        });

    }

    private void cambiarLosSitiosdeInteres() {
        for (MapLabeledMarker mLm:m_mapRecommendedList) {
            if(mLm.isVisible()) {
                mLm.setVisible(false);
            } else {
                mLm.setVisible(true);
            }
        }
    }

    private void initMapFragment() {
        m_mapFragment = getMapFragment();
        //MapEngine.getInstance().setOnline(true);
        String diskCacheRoot = m_activity.getFilesDir().getPath()
                + File.separator + ".isolated-here-maps";
        String intentName = "";
        try {
            ApplicationInfo ai = m_activity.getPackageManager().getApplicationInfo(m_activity.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            intentName = bundle.getString("INTENT_NAME");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(this.getClass().toString(), "Failed to find intent name, NameNotFound: " + e.getMessage());
        }

        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(diskCacheRoot, intentName);
        if (!success) {
        } else {
            if (m_mapFragment != null) {
                m_mapFragment.init(new OnEngineInitListener() {
                    @Override
                    public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                        if (error == Error.NONE) {
                            //Ajustes iniciales del mapa
                            m_map = m_mapFragment.getMap();

                            valencia = new GeoCoordinate(39.466667, -0.375000, 0.0);

                            m_map.setCenter(valencia, Map.Animation.NONE);
                            m_map.getMapTransitLayer().setMode(MapTransitLayer.Mode.STOPS_AND_ACCESSES);
                            m_map.setMapScheme(Map.Scheme.NORMAL_DAY);

                            m_map.setZoomLevel((m_map.getMaxZoomLevel() + m_map.getMinZoomLevel()) / 2);
                        } else {
                            new AlertDialog.Builder(m_activity).setMessage(
                                    "Error : " + error.name() + "\n\n" + error.getDetails())
                                    .setTitle(R.string.engine_init_error)
                                    .setNegativeButton(android.R.string.cancel,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    m_activity.finish();
                                                }
                                            }).create().show();
                        }
                    }
                });
            }
        }
    }

    private void createRoute() {
        CoreRouter coreRouter1 = new CoreRouter();
        CoreRouter coreRouter2 = new CoreRouter();

        RouteOptions routeOptions = new RouteOptions();
        //Que solo sea transporte publico
        routeOptions.setTransportMode(RouteOptions.TransportMode.PUBLIC_TRANSPORT);
        //Desactivamos otras opciones que no sean bus
        routeOptions.setPublicTransportTypeAllowed(TransitType.RAIL_METRO, false);
        routeOptions.setPublicTransportTypeAllowed(TransitType.RAIL_LIGHT, false);
        routeOptions.setPublicTransportTypeAllowed(TransitType.ORDERED_SERVICES_OR_TAXI, false);
        routeOptions.setHighwaysAllowed(false);
        routeOptions.setRouteType(RouteOptions.Type.SHORTEST);
        routeOptions.setRouteCount(2);
        //setTime idicar hora https://developer.here.com/documentation/android-premium/api_reference_java/com/here/android/mpa/routing/RouteOptions.html

        //Recuperamos los puntos del mapa
        List<PlaceLink> puntosMapaBien = Arrays.asList(puntosMapa);
        PlaceLink placeOrigin = puntosMapaBien.get(0);
        PlaceLink placeInterest = puntosMapaBien.get(1);

        PlaceLink placeArrival = puntosMapaBien.get(2);

        RouteWaypoint startPoint = new RouteWaypoint(new GeoCoordinate(placeOrigin.getPosition()));
        RouteWaypoint middlePoint = new RouteWaypoint(new GeoCoordinate(placeInterest.getPosition()));
        RouteWaypoint endPoint = new RouteWaypoint(new GeoCoordinate(placeArrival.getPosition()));

        RouteOptions routeOptionsInicio = new RouteOptions(routeOptions);
        //Calculamos la primera ruta
        RoutePlan routePlanInicio = new RoutePlan();
        routeOptionsInicio.setTime(horaInicio, RouteOptions.TimeType.DEPARTURE);
        routePlanInicio.setRouteOptions(routeOptionsInicio);
        routePlanInicio.addWaypoint(startPoint);
        routePlanInicio.addWaypoint(middlePoint);

        execRuta(coreRouter1, routePlanInicio, false);

        //Calculamos la segunda ruta
        RoutePlan routePlanVuelta = new RoutePlan();
        routeOptions.setTime(horaFin, RouteOptions.TimeType.DEPARTURE);
        routePlanVuelta.setRouteOptions(routeOptions);
        routePlanVuelta.addWaypoint(middlePoint);
        routePlanVuelta.addWaypoint(endPoint);

        execRuta(coreRouter2, routePlanVuelta, true);
    }

    private void getGeoCoordinateFrom(String toString, int id) {
        SearchRequest searchRequest = new SearchRequest(toString);
        searchRequest.setSearchCenter(m_map.getCenter());
        searchRequest.execute(discoveryResultPageListener(id));
    }

    private synchronized ResultListener<DiscoveryResultPage> discoveryResultPageListener(final int idIndex) {
        ResultListener<DiscoveryResultPage> discoveryResultPageListener = new ResultListener<DiscoveryResultPage>() {
            @Override
            public void onCompleted(DiscoveryResultPage discoveryResultPage, ErrorCode errorCode) {
                if (errorCode == ErrorCode.NONE) {
                    s_ResultList = discoveryResultPage.getItems();
                    DiscoveryResult item = s_ResultList.get(0);
                    if (item.getResultType() == DiscoveryResult.ResultType.PLACE) {
                        PlaceLink placeLink = (PlaceLink) item;
                        System.out.println(placeLink.getTitle());
                        addMarkerAtPlace(placeLink, idIndex);
                    }
                } else {
                    Toast.makeText(m_activity,
                            "ERROR:Discovery search request returned return error code+ " + errorCode,
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        return discoveryResultPageListener;
    }

    private void addMarkerAtPlace(PlaceLink placeLink, int id) {
        Image img = new Image();
        try {
            img.setImageResource(R.drawable.green_dot);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MapLabeledMarker mapMarker = new MapLabeledMarker(new GeoCoordinate(placeLink.getPosition()));
        mapMarker.setIcon(img);
        m_map.addMapObject(mapMarker);
        placesRoute.add(placeLink);
        m_mapObjectList.add(mapMarker);
        puntosMapa[id] = placeLink;
        if(id == 0){
            mapMarkerInicio=mapMarker;
        } else if (id ==1){
            mapMarkerFin=mapMarker;
        }
        //System.out.println(placeLink.getTitle() + " " + id);
    }

    private void addRecommendedAtPlace(PlaceLink placeLink) {
        Image img = new Image();
        try {
            Category category = placeLink.getCategory().getParent();
            if (Category.Global.ACCOMMODATION.toString().equals(category.getId())) {
                img.setImageResource(R.drawable.red_dot);
            } else if (Category.Global.EAT_DRINK.toString().equals(category.getId())) {
                img.setImageResource(R.drawable.blue_dot);
            } else if (Category.Global.GOING_OUT.toString().equals(category.getId())) {
                img.setImageResource(R.drawable.orange_dot);
            } else if (Category.Global.LEISURE_OUTDOOR.toString().equals(category.getId())) {
                img.setImageResource(R.drawable.pink_dot);
            } else if (Category.Global.NATURAL_GEOGRAPHICAL.toString().equals(category.getId())) {
                img.setImageResource(R.drawable.turk_dot);
            } else if (Category.Global.SHOPPING.toString().equals(category.getId())) {
                img.setImageResource(R.drawable.yellow_dot);
            } else if (Category.Global.SIGHTS_MUSEUMS.toString().equals(category.getId())) {
                img.setImageResource(R.drawable.purple_dot);
            } else {
                img.setImageResource(R.drawable.brown_dot);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        MapLabeledMarker mapMarker = new MapLabeledMarker(new GeoCoordinate(placeLink.getPosition()),img);
        //mapMarker.setIcon(img);
            //mapMarker.setCoordinate(new GeoCoordinate(placeLink.getPosition()));
        mapMarker.setLabelText("spa",placeLink.getTitle());

        m_map.addMapObject(mapMarker);
        placesRoute.add(placeLink);
        m_mapObjectList.add(mapMarker);
        m_mapRecommendedList.add(mapMarker);
    }


    private void createWaypoints() {
        for (MapObject mapObject : m_mapObjectList) {
            m_map.removeMapObject(mapObject);
        }
        getGeoCoordinateFrom(textOrigin.getText().toString(),0);
        getGeoCoordinateFrom(textInterest.getText().toString(),1);
        getGeoCoordinateFrom(textArrival.getText().toString(),2);

        String day =
                String.format("%02d", new Date(System.currentTimeMillis()).getDay() + 2).concat(
                String.format("%02d", new Date(System.currentTimeMillis()).getMonth() + 1).concat(
                String.format("%02d", new Date(System.currentTimeMillis()).getYear() + 1900)));
        DateFormat formatter = new SimpleDateFormat("ddMMyyyy HH:mm");

        try {
            horaInicio = formatter.parse(day + " " + fechaInicio.getText().toString());
            horaFin = formatter.parse(day + " " + fechaFin.getText().toString());
            horaFin = addMinutesToDate(30, horaFin);

            //System.out.println(horaFin);
            //System.out.println(horaInicio);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static Date addMinutesToDate(int minutes, Date beforeTime){
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = new Date(curTimeInMs - (minutes * ONE_MINUTE_IN_MILLIS));
        return afterAddingMins;
    }

    private void createIsoline() {
        int radioMax = 250;
        if(fechaFinAux.size() != 0) {
            Date fechaCorrecta = fechaFinAux.get(0);
            System.out.println("este bus: " + busVuelta);
            mapMarkerFin.setLabelText("spa", "Bus: " + busVuelta + " a " + fechaCorrecta.getHours() + "h y " + fechaCorrecta.getMinutes()+ "m");
            mapMarkerFin.setFontScalingFactor(5);
        }
        if(fechaIniAux.size() != 0) {
            Date fechaCorrecta = fechaIniAux.get(0);
            System.out.println("este bus: " + busIda);
            mapMarkerInicio.setLabelText("spa", "Bus: " + busIda + " a las " + fechaCorrecta.getHours() + "h y " + fechaCorrecta.getMinutes()+ "m");
            mapMarkerInicio.setFontScalingFactor(5);
        }
        ExploreRequest expReq = new ExploreRequest();
        CategoryFilter catFilter = PreferencesActivity.selectedCategories;

        expReq.setConnectivity(Request.Connectivity.ONLINE);
        expReq.setSearchArea(puntosMapa[1].getPosition(),radioMax);
        expReq.setCategoryFilter(catFilter);
        expReq.setCollectionSize(10);

        expReq.execute(new ResultListener<DiscoveryResultPage>() {
            @Override
            public void onCompleted(DiscoveryResultPage discoveryResultPage, ErrorCode errorCode) {
                if (errorCode == ErrorCode.NONE) {
                    s_ResultList = discoveryResultPage.getItems();
                    //DiscoveryResult item = s_ResultList.get(0);
                    for (DiscoveryResult item: s_ResultList) {
                        if (item.getResultType() == DiscoveryResult.ResultType.PLACE) {
                            PlaceLink placeLink = (PlaceLink) item;
                            addRecommendedAtPlace(placeLink);
                            //placeLink.getCategory()
                        }
                    }

                } else {
                    Toast.makeText(m_activity,
                            "ERROR:Discovery search request returned return error code+ " + errorCode,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void execRuta(CoreRouter coreRouter, RoutePlan route, final boolean esFin){
        coreRouter.calculateRoute(route,
                new Router.Listener<List<RouteResult>, RoutingError>() {
                    @Override
                    public void onProgress(int i) {
                        /* The calculation progress can be retrieved in this callback. */
                    }

                    @Override
                    public void onCalculateRouteFinished(List<RouteResult> routeResults,
                                                         RoutingError routingError) {
                        if (routingError == RoutingError.NONE) {
                            if (routeResults.get(0).getRoute() != null) {
                                Route rutaAux = routeResults.get(0).getRoute();
                                MapRoute m_mapRoute = new MapRoute(routeResults.get(0).getRoute());
                                Date tiempo = null;
                                if(esFin){
                                    for (RouteResult rutaResultFor: routeResults) {
                                        Route rutaHelp = rutaResultFor.getRoute();
                                        for (RouteElement routeElement:rutaHelp.getRouteElements().getElements()) {
                                            if(routeElement.getType() == RouteElement.Type.TRANSIT){
                                                if(routeElement.getTransitElement().getTransitType() == TransitType.BUS_PUBLIC) {
                                                    tiempo = routeElement.getTransitElement().getDepartureTime();
                                                    fechaFinAux.add(tiempo);
                                                    busVuelta = routeElement.getTransitElement().getLineName();
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    for (RouteResult rutaResultFor: routeResults) {
                                        Route rutaHelp = rutaResultFor.getRoute();
                                        for (RouteElement routeElement:rutaHelp.getRouteElements().getElements()) {
                                            if(routeElement.getType() == RouteElement.Type.TRANSIT){
                                                if(routeElement.getTransitElement().getTransitType() == TransitType.BUS_PUBLIC) {
                                                    tiempo = routeElement.getTransitElement().getArrivalTime();
                                                    busIda = routeElement.getTransitElement().getLineName();
                                                    fechaIniAux.add(tiempo);
                                                }
                                            }
                                        }
                                    }
                                }
                                m_mapRoute.setManeuverNumberVisible(true);
                                /*for (Maneuver rutaMan: rutaAux.getManeuvers()) {
                                    for (RouteElement reEle : rutaMan.getRouteElements()) {
                                        if(reEle.getType() == RouteElement.Type.TRANSIT){

                                        }
                                    }
                                }*/

                                /* Add the MapRoute to the map */
                                m_map.addMapObject(m_mapRoute);
                                if(esFin){
                                    m_mapRoute.setColor(Color.argb(255,158,242,1));
                                } else {
                                    m_mapRoute.setColor(Color.argb(255,70,153,237));
                                }

                                addRouteToMapList(m_mapRoute);
                                /*
                                 * We may also want to make sure the map view is orientated properly
                                 * so the entire route can be easily seen.
                                 */
                                GeoBoundingBox gbb = routeResults.get(0).getRoute()
                                        .getBoundingBox();
                                m_map.zoomTo(gbb, Map.Animation.NONE,
                                        Map.MOVE_PRESERVE_ORIENTATION);
                            } else {
                                Toast.makeText(m_activity,
                                        "Error:route results returned is not valid",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(m_activity,
                                    "Error:route calculation returned error code: " + routingError,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /*private void checkiftodoresuelto(Date tiempo, boolean esFin) {
        if(!esFin){
            fechaIniAux = tiempo;
        } else {
            fechaFinAux = tiempo;
        }

        System.out.println("Entro 1 vez " + esFin);

        if(fechaIniAux != null && fechaFinAux != null){
            System.out.println("Estamos qui");
            if(fechaIniAux.after(fechaFinAux)){
                System.out.println("FechasMal");
            }
        }
    }*/

    private void setTiempoFin(int tiempo) {

    }

    private void addRouteToMapList(MapRoute m_mapRoute) {
        m_mapObjectList.add(m_mapRoute);
    }
}
