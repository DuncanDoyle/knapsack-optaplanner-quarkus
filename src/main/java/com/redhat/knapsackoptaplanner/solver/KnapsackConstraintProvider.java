package com.redhat.knapsackoptaplanner.solver;

import com.redhat.knapsackoptaplanner.domain.Ingot;
import com.redhat.knapsackoptaplanner.domain.Knapsack;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class KnapsackConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] { 
            maxWeight(constraintFactory),
            maxValue(constraintFactory)
        };
    }

    /*
     * 
     * rule "Max Weight" when Knapsack($maxWeight: maxWeight) accumulate(
     * Ingot(selected == true, $weight: weight); $totalWeight : sum($weight);
     * $totalWeight > $maxWeight ) Ingot(selected == true)
     * 
     * then scoreHolder.penalize(kcontext, $totalWeight - $maxWeight); end
     */

    private Constraint maxWeight(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Ingot.class).filter(i -> i.getSelected())
                .groupBy(ConstraintCollectors.sum(i -> i.getWeight())).join(Knapsack.class)
                .filter((ws, k) -> ws > k.getMaxWeight())
                .penalize("Max Weight", HardSoftScore.ONE_HARD, (ws, k) -> ws - k.getMaxWeight());
    }

    /*
     * rule "Max Value" when Ingot(selected == true, $value: value) then
     * scoreHolder.reward(kcontext, $value); end
     */
    private Constraint maxValue(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Ingot.class)
                    .filter(Ingot::getSelected)
                    .reward("Max Value", HardSoftScore.ONE_SOFT, Ingot::getValue);
    }

}