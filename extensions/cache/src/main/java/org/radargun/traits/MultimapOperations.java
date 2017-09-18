package org.radargun.traits;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.radargun.Operation;

/**
 * Basic multimap operations
 * 
 * @author zhostasa
 *
 */
@Trait(doc = "Cache multimap operations")
public interface MultimapOperations {
   String TRAIT = MultimapOperations.class.getSimpleName();
   Operation GET = Operation.register(TRAIT + ".Get");
   Operation PUT = Operation.register(TRAIT + ".Put");
   Operation GET_ENTRY = Operation.register(TRAIT + ".GetEntry");
   Operation REMOVE = Operation.register(TRAIT + ".Remove");
   Operation REMOVE_ENTRY = Operation.register(TRAIT + ".RemoveEntry");
   Operation REMOVE_PREDICATE = Operation.register(TRAIT + ".RemovePredicate");
   Operation CONTAINS_KEY = Operation.register(TRAIT + ".ContainsKey");
   Operation CONTAINS_VALUE = Operation.register(TRAIT + ".ContainsValue");
   Operation CONTAINS_ENTRY = Operation.register(TRAIT + ".ContainsEntry");

   <K, V> MultimapCache<K, V> getMultimapCache(String cacheName);

   interface MultimapCache<K, V> {

      Collection<V> get(K key);

      void put(K key, V value);

      Optional<Map.Entry<K, Collection<V>>> getEntry(K key);

      Boolean remove(K key, V value);
      
      Boolean removeEntry(K key);

      void remove(Predicate<? super V> p);

      Boolean containsKey(K key);

      Boolean containsValue(V value);

      Boolean containsEntry(K key, V value);
   }
}
