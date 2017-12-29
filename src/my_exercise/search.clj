(ns my-exercise.search
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [hiccup.page :refer [html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [my-exercise.us-state :as us-state]))

(defn header [_]
  [:head
   [:meta {:charset "UTF-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1.0, maximum-scale=1.0"}]
   [:title "Upcoming elections"]
   [:link {:rel "stylesheet" :href "default.css"}]])

(defn display-results [election-name date]
  ; will have to amend this to display multiple results if there are many
  ; the date comes in as a not-very-readable timestamp so I'd like to parse it to display in a more readable way
  [:div {:class "current-elections-results"}
    [:h1 "Upcoming Elections"]
    [:li date " - " election-name]
  ]
  )

(defn make-request [ocd-state ocd-place]
  ; make request with query string params defined in get-ocds
  (def req (str "https://api.turbovote.org/elections/upcoming?district-divisions=" ocd-state "," ocd-place))
  ; get response as JSON
  (def resp (client/get req {:accept :json}))
  ; having trouble getting into the array but would love some help if you give me the opportunity to pair on this
  ; here's an idea for how to get name and date from JSON
  (def election-name (get-in resp [:body "description"]))
  (def date (get-in resp [:body "date"]))
  ; send election name and date to display results
  ; nothing will display because I haven't actually gotten this working
  (display-results election-name date)
)

(defn get-ocds [city state]
  ; use city and state to make query string params to use in API request
  (def ocd-state (str "ocd-division/country:us/state:" state))
  (def ocd-place (str "ocd-division/country:us/state:" state "/place:" city))
  (make-request ocd-state ocd-place)
  )

(defn parse-params [request]
  ; cast strings to lowercase and for cities with spaces in the name (ex. "Oak Lawn, IL", replace spaces with underscores (ex. oak_lawn))
  (def city (str/lower-case (clojure.string/replace (get (:form-params request) "city") #" " "_")))
  (def state (str/lower-case (get (:form-params request) "state")))
  ; save zip to use later in determining county etc.
  ; (def zip (get (:form-params request) "zip"))
  (get-ocds city state)
  )

(defn page [request]
  (html5
   (header request)
   (parse-params request)))
