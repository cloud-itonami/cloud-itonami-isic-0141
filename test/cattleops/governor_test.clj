(ns cattleops.governor-test
  (:require [clojure.test :refer [deftest testing is]]
            [cattleops.governor :as gov]
            [cattleops.store :as store]))

(deftest hard-violations-no-facility-id
  (testing "Hard violation: missing facility-id"
    (let [req {}
          prop {:op :log-herd-record :effect :propose}
          s (store/mem-store)
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (seq (:violations verdict))))))

(deftest hard-violations-unregistered-facility
  (testing "Hard violation: facility not registered"
    (let [req {:facility-id "farm-001"}
          prop {:op :log-herd-record :effect :propose}
          s (store/mem-store)
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :facility-not-registered (:rule %)) (:violations verdict))))))

(deftest hard-violations-effect-not-propose
  (testing "Hard violation: effect is not :propose"
    (let [facility {:id "farm-001" :name "Test Farm"}
          s (store/mem-store :facilities {"farm-001" facility})
          req {:facility-id "farm-001"}
          prop {:op :log-herd-record :effect :execute}
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :no-execution (:rule %)) (:violations verdict))))))

(deftest hard-violations-treatment-blocked
  (testing "Hard violation: treatment operation is blocked"
    (let [facility {:id "farm-001" :name "Test Farm"}
          s (store/mem-store :facilities {"farm-001" facility})
          req {:facility-id "farm-001"}
          prop {:op :administer-treatment :effect :propose}
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :treatment-or-slaughter-blocked (:rule %)) (:violations verdict))))))

(deftest hard-violations-slaughter-blocked
  (testing "Hard violation: slaughter operation is blocked"
    (let [facility {:id "farm-001" :name "Test Farm"}
          s (store/mem-store :facilities {"farm-001" facility})
          req {:facility-id "farm-001"}
          prop {:op :order-slaughter :effect :propose}
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :treatment-or-slaughter-blocked (:rule %)) (:violations verdict))))))

(deftest ok-herd-logging
  (testing "OK: valid herd record logging with registered facility"
    (let [facility {:id "farm-001" :name "Test Farm"}
          s (store/mem-store :facilities {"farm-001" facility})
          req {:facility-id "farm-001"}
          prop {:op :log-herd-record :effect :propose :count 50 :confidence 0.9}
          verdict (gov/check req nil prop s)]
      (is (:ok? verdict))
      (is (not (:hard? verdict)))
      (is (not (:escalate? verdict))))))

(deftest escalation-health-concern
  (testing "Escalation: animal health concern"
    (let [facility {:id "farm-001" :name "Test Farm"}
          s (store/mem-store :facilities {"farm-001" facility})
          req {:facility-id "farm-001"}
          prop {:op :flag-animal-health-concern
                :effect :propose
                :concern "疾病の可能性"
                :confidence 0.8}
          verdict (gov/check req nil prop s)]
      (is (not (:hard? verdict)))
      (is (:escalate? verdict)))))

(deftest escalation-low-confidence
  (testing "Escalation: low confidence"
    (let [facility {:id "farm-001" :name "Test Farm"}
          s (store/mem-store :facilities {"farm-001" facility})
          req {:facility-id "farm-001"}
          prop {:op :log-herd-record :effect :propose :count 50 :confidence 0.5}
          verdict (gov/check req nil prop s)]
      (is (not (:hard? verdict)))
      (is (:escalate? verdict)))))

(deftest escalation-supply-order-high-cost
  (testing "Escalation: supply order over cost threshold"
    (let [facility {:id "farm-001" :name "Test Farm"}
          s (store/mem-store :facilities {"farm-001" facility})
          req {:facility-id "farm-001"}
          prop {:op :order-supplies :effect :propose :cost 1000 :confidence 0.9}
          verdict (gov/check req nil prop s)]
      (is (not (:hard? verdict)))
      (is (:escalate? verdict)))))

(deftest ok-supply-order-low-cost
  (testing "OK: supply order under cost threshold"
    (let [facility {:id "farm-001" :name "Test Farm"}
          s (store/mem-store :facilities {"farm-001" facility})
          req {:facility-id "farm-001"}
          prop {:op :order-supplies :effect :propose :cost 100 :confidence 0.9}
          verdict (gov/check req nil prop s)]
      (is (:ok? verdict))
      (is (not (:escalate? verdict))))))
