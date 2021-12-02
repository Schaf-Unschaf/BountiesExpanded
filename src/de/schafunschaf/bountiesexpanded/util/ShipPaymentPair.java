package de.schafunschaf.bountiesexpanded.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ShipPaymentPair<A, B> {

    public A ship;
    public B payment;
    private boolean conditionFulfilled;

    public ShipPaymentPair(A ship, B payment) {
        super();
        this.ship = ship;
        this.payment = payment;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((ship == null) ? 0 : ship.hashCode());
        result = PRIME * result + ((payment == null) ? 0 : payment.hashCode());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ShipPaymentPair<A, B> other = (ShipPaymentPair<A, B>) obj;
        if (ship == null) {
            if (other.ship != null)
                return false;
        } else if (!ship.equals(other.ship))
            return false;
        if (payment == null)
            return other.payment == null;
        else
            return payment.equals(other.payment);
    }

}
