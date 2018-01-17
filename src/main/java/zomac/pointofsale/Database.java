package zomac.pointofsale;

import java.util.Optional;

public interface Database {

    Optional<Product> find(String id);
}
