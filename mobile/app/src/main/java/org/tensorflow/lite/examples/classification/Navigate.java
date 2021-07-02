package org.tensorflow.lite.examples.classification;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;


public class Navigate {
    //경로생성, 진행방향 제시
    Context mContext;
    Astar astar;

    ArrayList<Node> route;

    int i = 0;

    public Navigate(Context context) {
        mContext = context;
        astar = new Astar(context);
    }

    public void createMap() {
        astar.createMap();
    }

    public void clearAll() {
        astar = null;
        route = null;
        i = 0;
    }

    public void setRoute(String currentLocation, String destination) {
        route = astar.getRoute(currentLocation, destination);
    }

    public String getRouteStr() {
        String s = "";
        for (Node n : route) {
            s += n.locationInfo;
        }
        return s;
    }

    public String nextDirection() {
        i++;

        if (i+1 == route.size()) {
            return "도착";
        }

        return getDirection(route.get(i - 1), route.get(i), route.get(i + 1));
    }

    private String getDirection(Node pastLocation, Node currentLocation, Node nextLocation) {
        int pastX = pastLocation.getX(), pastY = pastLocation.getY();
        int x1 = currentLocation.getX(), y1 = currentLocation.getY();
        int x2 = nextLocation.getX(), y2 = nextLocation.getY();

        //내가 변환한 cell 값이랑 tm_x tm_y 값이랑 반대방향? 이라서 tm_x tm_y로 바꿀 땐 음수가 왼쪽, 양수가 오른쪽이어야 함.
        if (getAngle(pastX, pastY, x1, y1, x2, y2) < 0) {
            if (-getAngle(pastX, pastY, x1, y1, x2, y2) < 10) return "직진";
            else return "왼쪽";
        } else {
            if (getAngle(pastX, pastY, x1, y1, x2, y2) < 10) return "직진";
            else return "오른쪽";
        }
    }

    private double getAngle(double pastX, double pastY, double x1, double y1, double x2, double y2) {
        double past_x = pastX, past_y = pastY;
        double current_x = x1, current_y = y1;
        double to_x = x2, to_y = y2;

        double go_x = current_x - past_x;
        double go_y = current_y - past_y;

        double to_dx = to_x - current_x;
        double to_dy = to_y - current_y;

        double location_degree = Math.toDegrees(Math.atan2(go_x, go_y));
        double location_degree_rad = Math.toRadians(location_degree);

        double dx = to_dx * Math.cos(location_degree_rad) - to_dy * Math.sin(location_degree_rad);
        double dy = to_dx * Math.sin(location_degree_rad) + to_dy * Math.cos(location_degree_rad);

        double degree = Math.toDegrees(Math.atan2(dx, dy));

        return degree;
    }

    private class Astar {
        private PriorityQueue<Node> openList;
        private ArrayList<Node> closedList;
        HashMap<Node, Integer> gMaps;
        HashMap<Node, Integer> fMaps;
        private int initialCapacity = 100;

        public ArrayList<Node> n = new ArrayList<>();

        Context mContext;

        public Astar(Context context) {
            gMaps = new HashMap<Node, Integer>();
            fMaps = new HashMap<Node, Integer>();
            openList = new PriorityQueue<Node>(initialCapacity, new fCompare());
            closedList = new ArrayList<Node>();

            this.mContext = context;
        }

        private void createMap() {
            String DB_NAME = "/test.db";
            String TB_NAME = "JANGJEON";
            DBAdapter db = new DBAdapter(mContext, DB_NAME);

            String sql = String.format("select ID, X, Y, LOCATION_INFO, NEIGHBORS from %s", TB_NAME);
            Cursor cursor = db.search(sql);

            int i = 0;

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex("ID"));
                    int x = cursor.getInt(cursor.getColumnIndex("X"));
                    int y = cursor.getInt(cursor.getColumnIndex("Y"));
                    String locationInfo = cursor.getString(cursor.getColumnIndex("LOCATION_INFO"));

                    String[] neighborsIds = cursor.getString(cursor.getColumnIndex("NEIGHBORS")).split("/");
                    n.add(new Node(id, x, y, locationInfo.replace(" ", ""), neighborsIds));
                }
                cursor.close();
            }

            for (int j = 0; j < n.size(); j++) {
                for (String id : n.get(j).getNeighborsIds()) {
                    int nId = Integer.parseInt(id);

                    for (int k = 0; k < n.size(); k++) {
                        if (n.get(k).getId() == nId) {
                            n.get(j).addNeighbor(n.get(k));
                        }
                    }
                }
            }

            try {
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<Node> getRoute(String currentLocation, String destination) {
            Log.v("BORA", "시작 : " + currentLocation);
            Log.v("BORA", "끝 : " + destination);
            Node start = this.findLocationInfo(currentLocation).get(0);
            Node end = this.findEndLocation(start, this.findLocationInfo(destination));

            Log.d("Bora", "찾았따 " + start.getLocationInfo() + "," + end.getLocationInfo());

            if (start == null || end == null) {
                return null;
            }

            openList.clear();
            closedList.clear();

            gMaps.put(start, 0);
            openList.add(start);

            while (!openList.isEmpty()) {
                Node current = openList.element();

                if (current.equals(end)) {
                    Log.d("BORA", "SUCCESS ASTAR");

                    ArrayList<Node> route = new ArrayList<>();

                    route.add(current);
                    while (current.getParent() != null) {
                        current = current.getParent();
                        route.add(current);
                    }

                    Collections.reverse(route);

                    return route;
                }

                closedList.add(openList.poll());
                ArrayList<Node> neighbors = current.getNeighbors();

                for (Node neighbor : neighbors) {
                    int gScore = gMaps.get(current) + h(neighbor, current); //실제 거리
                    int fScore = gScore + h(neighbor, end); //시작노드 ~ 목표노드까지 추정 거리

                    if (closedList.contains(neighbor)) {
                        if (gMaps.get(neighbor) == null) {
                            gMaps.put(neighbor, gScore);
                        }
                        if (fMaps.get(neighbor) == null) {
                            fMaps.put(neighbor, fScore);
                        }

                        if (fScore >= fMaps.get(neighbor)) {
                            continue;
                        }
                    }
                    if (!openList.contains(neighbor) || fScore < fMaps.get(neighbor)) {
                        neighbor.setParent(current);
                        gMaps.put(neighbor, gScore);
                        fMaps.put(neighbor, fScore);
                        if (!openList.contains(neighbor)) {
                            openList.add(neighbor);

                        }
                    }
                }
            }

            Log.d("BORA", "NO PATH");
            return null;
        }

        //어떤 거리 알고리즘 쓸지에 따라 정확성, 속도 등 달라짐
        private int h(Node node, Node goal) {
            int x = node.getX() - goal.getX();
            int y = node.getY() - goal.getY();
            return x * x + y * y;
        }

        public ArrayList<Node> findLocationInfo(String locationInfo) {
            ArrayList<Node> nlist = new ArrayList<>();

            Log.d("Bora", "----------");
            Log.d("Bora", "어쩌고 : " + locationInfo);
            Log.d("Bora", "----------");
            for (int i = 0; i < n.size(); i++) {
                if (n.get(i).getLocationInfo().equals(locationInfo.replace(" ", ""))) {
                    nlist.add(n.get(i));
                }
            }

            if(n.size() <= 0) {
                Log.d("BORA", "find location info 실패");
                return null;
            } else {
                return nlist;
            }
        }

        public Node findEndLocation(Node start, ArrayList<Node> nlist) {
            int min_d = 9999;
            int d;
            Node result = null;

            if(nlist.size() == 1) {
                return nlist.get(0);
            }

            for (Node n : nlist) {
                d = h(start, n);

                if (d <= min_d) {
                    result = n;
                }
            }
            
            return result;
        }

            class fCompare implements Comparator<Node> {

                public int compare(Node o1, Node o2) {
                    if (fMaps.get(o1) < fMaps.get(o2)) {
                        return -1;
                    } else if (fMaps.get(o1) > fMaps.get(o2)) {
                        return 1;
                    } else {
                        return 1;
                    }
                }
            }
        }

        private class Node {
            private Node parent;
            private ArrayList<Node> neighbors;

            private int id;
            private int x;
            private int y;
            private String locationInfo;
            private String[] neighborsIds;

            public Node(int id, int x, int y, String locationInfo, String[] neighborsIds) {
                neighbors = new ArrayList<Node>();

                this.id = id;
                this.x = x;
                this.y = y;
                this.locationInfo = locationInfo;
                this.neighborsIds = neighborsIds;
            }

            public ArrayList<Node> getNeighbors() {
                return neighbors;
            }

            public void setNeighbors(ArrayList<Node> neighbors) {
                this.neighbors = neighbors;
            }

            public void addNeighbor(Node node) {
                this.neighbors.add(node);
            }

            public void addNeighbors(Node... node) {
                this.neighbors.addAll(Arrays.asList(node));
            }

            public Node getParent() {
                return parent;
            }

            public void setParent(Node parent) {
                this.parent = parent;
            }

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }

            public int getId() {
                return id;
            }

            public String[] getNeighborsIds() {
                return neighborsIds;
            }

            public String getLocationInfo() {
                return locationInfo;
            }

            public boolean equals(Node n) {
                return this.x == n.x && this.y == n.y;
            }
        }
    }