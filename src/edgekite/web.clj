(ns edgekite.web
  (:use [edgekite.routing]
        [edgekite.logging]
        [ring.middleware.resource]
        [ring.middleware.file-info]
        [ring.middleware.params]
        [ring.util.response]
        [ring.handler.dump]
        [hiccup.core]
        [hiccup.page])
  (:require [gudu]))

(defn page [title & body]
  (html
   (html5
    [:html
     [:head
      [:title "edgekite"]
      (include-css (gu :style))]
     [:body
      [:div#wrapper
       [:div#content
        [:div#top-bar
         [:h1  "edgekite"]]
        (if title [:h2 title])
        body
        (comment [:div (java.util.Date.)])]]
      (comment [:script "setTimeout(\"location = location\", 1000)"])]])))

(defn ok-html [body]
  (-> (response body)
      (content-type "text/html")))

(defn home [req]
  (ok-html (page nil [:div (str "Welcome to edgekite.")])))

(def hello-form
  [:form {:action (gu :hello)
          :method :post}
   [:label "Name:"]
   [:input {:name "name"}]
   [:input {:type "submit"}]])

(defn hello [req]
  (let [name (get-in req [:params "name"] "Unknown")
        body (page "hello" [:div (str "Hello " name) hello-form])]
    (ok-html body)))

(defn four-oh-four [req]
  (-> (page "Four, Oh! Four." "Errrm...")
      not-found
      (content-type "text/html")))

(defn map->table [m]
  [:table
     (map #(do [:tr [:td (first %)] [:td (str (second %))]]) (seq m))])

(defn log [req]
  (let [render-log-by (fn [id] [:div [:h3 (name id)] (map->table (get-log-by id))])
        body (page "log" (map render-log-by [:ip :url :date]))]
    (ok-html body)))

(def handlers
  {[:home]  home
   [:hello] hello
   [:log]   log
   [:debug] handle-dump
   :default four-oh-four})

(def handler
  (-> (router handlers)
      (wrap-resource "public")
      wrap-file-info
      wrap-params
      wrap-log))
