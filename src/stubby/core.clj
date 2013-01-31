(ns stubby.core
  (:use [compojure.core]
        [slingshot.slingshot :only [try+]])
  (:require [cheshire.core :as cheshire]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.tools.logging :as log]))

;Refs that store the provenance info
(def uuid-lookup (ref (hash-map)))
(def object-store (ref (hash-map)))
(def prov-logs (ref (hash-map)))

(defn get-uuid
  "Takes in a string containing the service object identifier and returns the
   faked UUID assigned to it."
  [service-object-id]
  (get-in @uuid-lookup [service-object-id :uuid]))

;Helper that turns a map into a JSON string.
(def jsonify (comp cheshire/encode hash-map))

(defn resp
  "Helper function that creates a response map."
  [status body]
  (hash-map :status status :body body))

(defn lookup-map
  "Creates a hash map for the info that's stored for lookups."
  [id name desc uuid parent]
  (hash-map
    :service_object_id id
    :object_name name
    :object_description desc
    :uuid uuid
    :parent_uuid parent))

(defn lookup
  "Performs a lookup of service-object-id. Returns its UUID if it can, a 404
   otherwise."
  [service-object-id]
  (println (str "LOOKUP: " service-object-id))
  (if (contains? @uuid-lookup service-object-id)
    (resp 200 (jsonify :UUID (get-uuid service-object-id)))
    (resp 404 (jsonify :Status "Failed"))))

(defn do-register
  "Adds a entry to the reffed hash-map for the provided id."
  [id oname desc parent]
  (let [new-uuid (str (java.util.UUID/randomUUID))
        new-map (lookup-map id oname desc new-uuid parent)]
    (dosync
      (ref-set uuid-lookup (assoc @uuid-lookup id new-map)))
    (resp 200 (jsonify :UUID new-uuid))))

(defn register
  "Figures out whether to call (do-register) or return an error response."
  [id oname desc parent]
  (try+
    (println (str "REGISTER: " id ))
    (if (contains? @uuid-lookup id)
      (resp 200 (jsonify :UUID (get-uuid id)))
      (do-register id oname desc parent))
    (catch Exception e
      (resp 500 (jsonify :Status "Failed")))))

(defn record-log
  "Records an event for the object associated with uuid."
  [uuid log-map]
  (dosync
    (ref-set prov-logs
             (assoc @prov-logs uuid (conj (get @prov-logs uuid) log-map)))))

(defn create-log
  "Creates a log seq for uuid in the prov-logs ref."
  [uuid]
  (dosync (ref-set prov-logs (assoc @prov-logs uuid []))))

(def success-result
  {:Status "Success"
   :Details "Provenance recorded"})

(def failed-result
  {:Status "Failed"
   :Details "Provenance was not recorded. Audit data recorded."})

(defn prov-log
  "Decides whether to create the log ref and whether to return an error
   or log the event."
  [uuid log-map]
  (try+
    (println (str "LOG: " uuid " EVENT MAP: " (cheshire/encode log-map)))
    (when-not (contains? @prov-logs uuid)
      (create-log uuid))
    (record-log uuid log-map)
    (resp 200 (jsonify :result success-result))
    (catch Exception e
      (resp 500 (jsonify :result failed-result)))))

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
       (prov-log
         uuid
         (hash-map :version version
                   :uuid uuid
                   :username username
                   :service_name service_name
                   :event_name event_name
                   :category_name category_name
                   :request_ipaddress request_ipaddress
                   :proxy_user_id proxy_user_id
                   :event_data event_data)))

  (GET "/" [] "STUUUUUUUUUBBY!")
  (route/not-found "Page not found!"))

(def app
  (handler/site stubby))
