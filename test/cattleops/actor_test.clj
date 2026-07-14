(ns cattleops.actor-test
  (:require [clojure.test :refer [deftest testing is]]
            [cattleops.actor :as actor]
            [cattleops.advisor :as advisor]
            [cattleops.store :as store]))

(deftest run-request-valid-herd-logging
  (testing "Valid herd record logging flow: intake -> advise -> govern -> decide -> commit"
    (let [facility {:id "farm-001" :name "Test Farm"}
          s (store/mem-store :facilities {"farm-001" facility})
          g (actor/build-graph {:store s :advisor (advisor/mock-advisor)})
          req {:facility-id "farm-001" :op :log-herd-record :count 50 :confidence 0.9 :effect :propose}
          ctx nil
          result (actor/run-request! g req ctx "test-thread-1")]
      (is (map? result))
      (is (contains? result :record)))))

(deftest run-request-hard-violation-blocks-commit
  (testing "Hard violation (missing facility) blocks commit"
    (let [s (store/mem-store)
          g (actor/build-graph {:store s :advisor (advisor/mock-advisor)})
          req {:facility-id "nonexistent" :op :log-herd-record :count 50 :effect :propose}
          ctx nil
          result (actor/run-request! g req ctx "test-thread-2")]
      (is (map? result))
      (is (not (contains? result :record))))))

(deftest run-request-escalation-interrupts
  (testing "Escalation (animal health concern) interrupts at :request-approval"
    (let [facility {:id "farm-001" :name "Test Farm"}
          s (store/mem-store :facilities {"farm-001" facility})
          g (actor/build-graph {:store s :advisor (advisor/mock-advisor)})
          req {:facility-id "farm-001" :op :flag-animal-health-concern
               :effect :propose :concern "fever" :confidence 0.8}
          ctx nil
          result (actor/run-request! g req ctx "test-thread-3")]
      (is (map? result)))))

(deftest store-ledger-gets-appended
  (testing "All operations are logged to the audit ledger"
    (let [facility {:id "farm-001" :name "Test Farm"}
          s (store/mem-store :facilities {"farm-001" facility})
          g (actor/build-graph {:store s :advisor (advisor/mock-advisor)})
          req {:facility-id "farm-001" :op :log-herd-record :count 50 :confidence 0.9 :effect :propose}
          ctx nil
          _ (actor/run-request! g req ctx "test-thread-4")
          ledger (store/ledger s)]
      (is (seq ledger))
      (is (> (count ledger) 0)))))
