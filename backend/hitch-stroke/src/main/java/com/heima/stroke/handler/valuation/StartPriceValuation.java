package com.heima.stroke.handler.valuation;

public class StartPriceValuation implements Valuation {
    private Valuation valuation;

    public StartPriceValuation(Valuation valuation) {
        this.valuation = valuation;
    }

    @Override
    public float calculation(float km) {
        if (km <= 3) {
            return valuation.calculation(km);
        }
        return (float) (valuation.calculation(km) + (km - 3) * 2.3);
    }
}
