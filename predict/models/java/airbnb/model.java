public void initialize(Map<String, Object> args) {
}

public Object predict(Map<String, Object> inputs) {
    Double bathrooms = (Double)inputs.get("bathrooms");

    Double bedrooms = (Double)inputs.get("bedrooms");

    Double security_deposit = (Double)inputs.get("security_deposit");

    Double cleaning_fee = (Double)inputs.get("cleaning_fee");

    Double extra_people = (Double)inputs.get("extra_people");

    Double number_of_reviews = (Double)inputs.get("number_of_reviews");

    Double square_feet = (Double)inputs.get("square_feet");

    Double review_scores_rating = (Double)inputs.get("review_scores_rating");

    String room_type = (String)inputs.get("room_type");

    String host_is_super_host = (String)inputs.get("host_is_super_host");

    String cancellation_policy = (String)inputs.get("cancellation_policy");

    String instant_bookable = (String)inputs.get("instant_bookable");

    String state = (String)inputs.get("state");

    Double price = (Double)inputs.get("price");
    Double __regressionTableNumber00 = -31.985533969928042;
    if (room_type != null) {
        __regressionTableNumber00 += 25.89385313144676 * ((room_type.equals("Entire home/apt")) ? 1 : 0);
    }
    if (room_type != null) {
        __regressionTableNumber00 += -13.556162863521244 * ((room_type.equals("Private room")) ? 1 : 0);
    }
    if (host_is_super_host != null) {
        __regressionTableNumber00 += -5.761601400860295 * ((host_is_super_host.equals("0.0")) ? 1 : 0);
    }
    if (cancellation_policy != null) {
        __regressionTableNumber00 += 2.3566340923908315 * ((cancellation_policy.equals("strict")) ? 1 : 0);
    }
    if (cancellation_policy != null) {
        __regressionTableNumber00 += -4.756763117856878 * ((cancellation_policy.equals("moderate")) ? 1 : 0);
    }
    if (cancellation_policy != null) {
        __regressionTableNumber00 += 0.0 * ((cancellation_policy.equals("flexible")) ? 1 : 0);
    }
    if (cancellation_policy != null) {
        __regressionTableNumber00 += 61.105365884755955 * ((cancellation_policy.equals("super_strict_30")) ? 1 : 0);
    }
    if (cancellation_policy != null) {
        __regressionTableNumber00 += 0.0 * ((cancellation_policy.equals("no_refunds")) ? 1 : 0);
    }
    if (cancellation_policy != null) {
        __regressionTableNumber00 += 48.979069203632854 * ((cancellation_policy.equals("super_strict_60")) ? 1 : 0);
    }
    if (instant_bookable != null) {
        __regressionTableNumber00 += 6.826451259656443 * ((instant_bookable.equals("0.0")) ? 1 : 0);
    }
    if (state != null) {
        __regressionTableNumber00 += -10.73912227061757 * ((state.equals("Other")) ? 1 : 0);
    }
    if (state != null) {
        __regressionTableNumber00 += 20.969294734441416 * ((state.equals("NY")) ? 1 : 0);
    }
    if (state != null) {
        __regressionTableNumber00 += 12.84035109618154 * ((state.equals("CA")) ? 1 : 0);
    }
    if (state != null) {
        __regressionTableNumber00 += -50.12288281511812 * ((state.equals("Berlin")) ? 1 : 0);
    }
    if (state != null) {
        __regressionTableNumber00 += 15.713453346118218 * ((state.equals("IL")) ? 1 : 0);
    }
    if (state != null) {
        __regressionTableNumber00 += 34.396875417378716 * ((state.equals("TX")) ? 1 : 0);
    }
    if (state != null) {
        __regressionTableNumber00 += -8.003145836426528 * ((state.equals("WA")) ? 1 : 0);
    }
    if (state != null) {
        __regressionTableNumber00 += 4.899311993759839 * ((state.equals("DC")) ? 1 : 0);
    }
    if (state != null) {
        __regressionTableNumber00 += -16.649400242261997 * ((state.equals("OR")) ? 1 : 0);
    }
    price = __regressionTableNumber00;

    return price;
}
