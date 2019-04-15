(ns hydrox.common.util-test
  (:use midje.sweet)
  (:require [hydrox.common.util :refer :all]
            [clojure.java.io :as io]))

^{:refer hydrox.common.util/full-path :added "0.1"}
(fact "constructs a path from a project"

  (full-path "example/file.clj" "src" {:root "/home/user"})
  => "/home/user/src/example/file.clj")

^{:refer hydrox.common.util/filter-pred :added "0.1"}
(fact "filters values of a map that fits the predicate"
  (filter-pred string? {:a "valid" :b 0})
  => {:a "valid"})

^{:refer hydrox.common.util/escape-dollars :added "0.1"}
(fact "for regex purposes, escape dollar signs in templates")

^{:refer hydrox.common.util/read-project :added "0.1"}
(fact "like `leiningen.core.project/read` but with less features'"

  (keys (read-project (io/file "example/project.clj")))
  => (just [:description :license :name :source-paths :test-paths
            :documentation :root :url :version :dependencies] :in-any-order))

^{:refer hydrox.common.util/deps-read-project :added "0.2"}
(fact "like `read-project` but for deps.edn'"

      (keys (deps-read-project (io/file "example/deps.edn")))
      => (just [:description :license :name :source-paths :test-paths
                :documentation :root :url :version :dependencies] :in-any-order))

(fact "project name is correctly found"
      (:name (deps-read-project (io/file "example/deps.edn")))
      => "example")

(fact "source paths do not include generated_src"
      (:source-paths (deps-read-project (io/file "example/deps.edn")))
      => ["src" "resources"])

(fact "test paths do not include generated_src"
      (:test-paths (deps-read-project (io/file "example/deps.edn")))
      => ["dev/resources"
          "test"
          "load-schema"
          "e2e-test"
          "fake/resources"
          "../datamodel/src"
          "../datamodel/resources"])

(fact "url is correctly formatted"
      (:url (deps-read-project (io/file "example/deps.edn")))
      => "github.com/parkside-securities/example")

(fact "all deps present"
      (count (keys (:dependencies (deps-read-project (io/file "example/deps.edn")))))
      => 7)
