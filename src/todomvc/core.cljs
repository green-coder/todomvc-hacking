(ns todomvc.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [todomvc.cofx]
            [todomvc.fx]
            [todomvc.interceptor]
            [todomvc.event]
            [todomvc.sub]
            [todomvc.router :as router]
            [todomvc.view :as view]))

(defn render []
  (rdom/render [view/main-page] (js/document.getElementById "app")))

(defn init []
  #_(println "(init)")
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch-sync [:local-storage/load-todo-items])
  (router/start-router!)
  (render)
  ,)

(defn ^:dev/before-load stop []
  #_(println "(stop)"))

(defn ^:dev/after-load start []
  #_(println "(start)")
  (rf/clear-subscription-cache!)
  (router/start-router!)
  (render))
