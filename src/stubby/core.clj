(ns stubby.core
  (:use [compojure.core])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.data.json :as json]))

(def uuid-lookup (ref (hash-map)))
(def object-store (ref (hash-map)))

(defn get-uuid
  [service-object-id]
  (get-in @uuid-lookup [service-object-id :uuid]))

(def jsonify (comp json/json-str hash-map))

(defn resp
  [status body]
  (hash-map :status status :body body))

(defn lookup-map
  [id name desc uuid parent]
  (hash-map
    :service_object_id id
    :object_name name
    :object_description desc
    :uuid uuid
    :parent_uuid parent))

(defn lookup
  [service-object-id]
  (if (contains? @uuid-lookup service-object-id)
    (resp 200 (jsonify :UUID (get-uuid service-object-id)))
    (resp 404 (jsonify :Status "Failed"))))

(defn register
  [id oname desc parent]
  (if (contains? @uuid-lookup id)
    (resp 200 (jsonify :UUID (get-uuid id)))
    (let [new-uuid (str (java.util.UUID/randomUUID))
          new-map (lookup-map id oname desc new-uuid parent)]
      (dosync
        (ref-set uuid-lookup (assoc @uuid-lookup id new-map)))
      (resp 200 (jsonify :UUID new-uuid)))))

(defroutes stubby
  (GET "/lookup/:version" 
       [version 
        service_object_id] 
       (lookup service_object_id))
  
  (GET "/register/:version" 
       [version 
        service_object_id 
        object_name 
        object_desc
        parent_uuid]
       (println object_name object_desc parent_uuid)
       (register service_object_id
                 object_name
                 object_desc
                 parent_uuid))
  
  (GET "/provenance/:version"
       [version
        uuid
        username
        service_name
        event_name
        category_name
        request_ipaddress
        proxy_user_id
        event_data]
       (str "version: " version
            "\nuuid: " uuid
            "\nusername: " username
            "\nservice_name: " service_name
            "\nevent_name: " event_name
            "\ncategory_name: " category_name
            "\nrequest_ipaddress: " request_ipaddress
            "\nproxy_user_id: " proxy_user_id
            "\nevent_data: " event_data))
  
  (GET "/" [] "STUUUUUUUUUBBY!")
  (route/not-found "Page not found!"))

(def app
  (handler/site stubby))
