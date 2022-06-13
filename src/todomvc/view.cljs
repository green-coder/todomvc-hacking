(ns todomvc.view
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rtfe]
            [todomvc.constant :as const]))

(defn todo-item-creation-input []
  (r/create-class
    {:reagent-render
     (fn []
       (let [title @(rf/subscribe [:comp.input/title])]
         [:input.new-todo {:type "text"
                           :value title
                           :placeholder "What needs to be done?"
                           :on-change (fn [event]
                                        (rf/dispatch [:comp.input/on-title-changed (-> event .-target .-value)]))
                           :on-key-down (fn [event]
                                          (let [key-pressed (.-which event)]
                                            (condp = key-pressed
                                              const/enter-keycode (rf/dispatch [:comp.input/add-todo-item title])
                                              nil)))}]))

     :component-did-mount
     (fn [this]
       (.focus (rdom/dom-node this)))}))


(defn toggle-items-button []
  (let [all-completed @(rf/subscribe [:all-todo-items-completed])]
    [:span
     [:input#toggle-all.toggle-all
      {:type "checkbox"
       :checked all-completed
       :on-change (fn [_]
                    (rf/dispatch [:toggle-all-todo-items all-completed]))}]
     [:label {:for "toggle-all"} "Mark all as complete"]]))


(defn todo-edit []
  (r/create-class
    {:reagent-render
     (fn [{:keys [id title]} editing]
       (let [default title
             edit-title (r/atom default)]
         (fn []
           [:input.edit {:type "text"
                         :style {:display (if @editing "inline" "none")}
                         :value @edit-title
                         :on-change (fn [event]
                                      (reset! edit-title (-> event .-target .-value)))
                         :on-blur (fn [_]
                                    (reset! editing false)
                                    (rf/dispatch [:set-todo-item-title id @edit-title]))
                         :on-key-down (fn [event]
                                        (let [key-pressed (.-which event)]
                                         (condp = key-pressed
                                           const/enter-keycode (do (reset! editing false)
                                                                   (rf/dispatch [:set-todo-item-title id @edit-title]))
                                           const/escape-keycode (do (reset! editing false)
                                                                    (reset! edit-title default))
                                           nil)))}])))

     :component-did-update
     (fn [x]
       (.focus (rdom/dom-node x)))}))

(defn todo-item [_todo-item-id]
  (let [editing (r/atom false)]
    (fn [todo-item-id]
      (let [{:keys [title completed] :as todo} @(rf/subscribe [:todo-item todo-item-id])]
        [:li {:class [(when completed "completed")
                      (when @editing "editing")]
              :style {:display (if @(rf/subscribe [:show-todo-item todo-item-id])
                                 "list-item"
                                 "none")}}
         [:div.view
          [:input.toggle {:type "checkbox"
                          :checked completed
                          :on-change (fn [_]
                                       (rf/dispatch [:toggle-todo-item todo-item-id]))}]
          [:label {:on-double-click (fn [_]
                                      (reset! editing true))}
           title]
          [:button.destroy {:on-click (fn [_]
                                        (rf/dispatch [:delete-todo-item todo-item-id]))}]]
         [todo-edit todo editing]]))))

(defn todo-items-list []
  (let [todo-item-ids @(rf/subscribe [:todo-item-ids])]
    [:ul.todo-list
     (for [todo-item-id todo-item-ids]
       ^{:key todo-item-id}
       [todo-item todo-item-id])]))

(defn todo-items-count []
  (let [active-todo-item-count @(rf/subscribe [:active-todo-items-count])]
    [:span.todo-count
     [:strong active-todo-item-count]
     (str (if (= active-todo-item-count 1) " item " " items ")
          "left")]))

(defn todo-items-filters []
  (let [display-type @(rf/subscribe [:comp/display-type])]
    [:ul.filters
     [:li [:a {:class [(when (= display-type :all) "selected")]
               :href (rtfe/href :page/all-todo-items)}
           "All"]]
     [:li [:a {:class [(when (= display-type :active) "selected")]
               :href (rtfe/href :page/active-todo-items)}
           "Active"]]
     [:li [:a {:class [(when (= display-type :completed) "selected")]
               :href (rtfe/href :page/completed-todo-items)}
           "Completed"]]]))

(defn clear-completed-button []
  [:button.clear-completed {:on-click (fn [_]
                                        (rf/dispatch [:clean-completed-todo-items]))
                            :style {:display (if @(rf/subscribe [:complemented-todo-items-to-clear])
                                               "inline"
                                               "none")}}
   "Clear completed"])

(defn main-page []
  [:div
   [:section.todoapp
    [:header.header
     [:h1 "todos"]
     [todo-item-creation-input]]
    [:div {:style
           {:display (if @(rf/subscribe [:comp.input/any-todo-item])
                       "inline"
                       "none")}}
     [:section.main
      [toggle-items-button]
      [todo-items-list]]
     [:footer.footer
      [todo-items-count]
      [todo-items-filters]
      [clear-completed-button]]]]
   [:footer.info
    [:p "Double-click to edit a todo"]]])
