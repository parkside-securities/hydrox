(ns hydrox.common.util
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn full-path
  "constructs a path from a project
 
   (full-path \"example/file.clj\" \"src\" {:root \"/home/user\"})
   => \"/home/user/src/example/file.clj\""
  {:added "0.1"} [path base project]
  (str (:root project) "/" base "/" path))

(defn filter-pred
  "filters values of a map that fits the predicate
   (filter-pred string? {:a \"valid\" :b 0})
   => {:a \"valid\"}"
  {:added "0.1"} [pred? m]
  (reduce-kv (fn [m k v] (if (pred? v)
                           (assoc m k v)
                           m))
             {} m))

(defn escape-dollars
  "for regex purposes, escape dollar signs in templates"
  {:added "0.1"}
  [s]
  (.replaceAll s "\\$" "\\\\\\$"))

(defn read-project
  "like `leiningen.core.project/read` but with less features'

   (keys (read-project (io/file \"example/project.clj\")))
   => (just [:description :license :name :source-paths :test-paths
             :documentation :root :url :version :dependencies] :in-any-order)"
  {:added "0.1"}
  ([] (read-project (io/file "project.clj")))
  ([file]
   (let [path  (.getCanonicalPath file)
         root  (subs path 0 (- (count path) 12))
         pform (read-string (slurp file))
         [_ name version] (take 3 pform)
         proj  (->> (drop 3 pform)
                    (concat [:name name
                             :version version
                             :root root])
                    (apply hash-map))]
     (-> proj
         (update-in [:source-paths] (fnil identity ["src"]))
         (update-in [:test-paths] (fnil identity ["test"]))))))

(defn deps-read-project
  "like `read-project` but for deps.edn'

   (keys (deps-read-project (io/file \"example/deps.edn\")))
   => (just [:description :license :name :source-paths :test-paths
             :documentation :root :url :version :dependencies] :in-any-order)"
  {:added "0.2"}
  ([] (read-project (io/file "deps.edn")))
  ([file]
   {:pre [(instance? java.io.File file)]}
   (let [path (.getCanonicalPath file)
         root  (subs path 0 (- (count path) 9))
         project-name (last (string/split root #"/"))
         url (str "github.com/parkside-securities/" project-name)
         proj-map (read-string (slurp file))
         dependencies (->> (:aliases proj-map)
                           (vals)
                           (map :extra-deps)
                           (reduce into (:deps proj-map)))
         source-paths (->> proj-map
                           :paths
                           (map (fnil identity "src"))
                           (remove #{"../datamodel/generated_src" "generated_src"})
                           vec)
         other-paths (->> proj-map
                          :aliases
                          vals
                          (mapcat :extra-paths)
                          (remove #{"../datamodel/generated_src" "generated_src"})
                          vec)
         test-paths (or (seq other-paths) ["test"])]
     {:name project-name
      :source-paths source-paths
      :test-paths test-paths
      :root root
      :description ""
      :license ""
      :documentation (:documentation proj-map)
      :url url
      :version ""
      :dependencies dependencies})))
