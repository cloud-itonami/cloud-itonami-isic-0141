(ns cattleops.facts
  "Reference facts for cattle-raising operations coordination: supply
  category cost policy and herd species classification. This namespace
  contains pure lookup functions for domain reference data -- the Governor
  and Advisor consult these instead of inventing thresholds. Mirrors
  `meatprocessing.facts` (cloud-itonami-isic-1010) in shape.")

(def supply-categories
  "Procurement categories this actor may propose orders for, and the
  default cost threshold above which an order proposal must escalate for
  human sign-off (rancher/ops-manager)."
  {"feed"
   {:id "feed" :name "飼料" :cost-threshold 500}

   "veterinary-supply"
   {:id "veterinary-supply" :name "獣医用品" :cost-threshold 500}

   "equipment"
   {:id "equipment" :name "設備" :cost-threshold 1000}})

(defn supply-category-by-id [id]
  (get supply-categories id))

(def default-cost-threshold
  "Fallback escalation threshold used when a supply-order proposal doesn't
  cite a known category (never invent a lower bar than this)."
  500)

(def species
  "Species this actor's facility/herd records may cover (ISIC 0141: cattle
  and buffaloes)."
  {"cattle"  {:id "cattle" :name "牛"}
   "buffalo" {:id "buffalo" :name "水牛"}})

(defn species-by-id [id]
  (get species id))
