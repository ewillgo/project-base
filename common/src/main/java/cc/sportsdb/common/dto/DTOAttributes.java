package cc.sportsdb.common.dto;

import java.io.Serializable;

public interface DTOAttributes<T> extends Serializable {
    /**
     * DTO object to PO object
     * @return
     */
    T toPO();
}
