package bigdata.movie.manager.controller;

import bigdata.movie.common.entity.Query;
import bigdata.movie.common.entity.ResponseEntity;
import bigdata.movie.manager.service.QueryService;
import bigdata.movie.manager.util.AppException;
import bigdata.movie.manager.vo.MovieDetail;
import bigdata.movie.manager.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class QueryController {

    private final QueryService queryService;

    @Autowired
    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/queries")
    public ResponseEntity<String> createQuery(@RequestBody Query query) throws AppException {
        return ResponseEntity.success(queryService.submitQuery(query));
    }

    @GetMapping("/queries/{queryId}/status")
    public ResponseEntity<String> getStatus(@PathVariable(value = "queryId") String queryId) throws AppException {
        return ResponseEntity.success(queryService.getQueryStatus(queryId));
    }

    @GetMapping("/queries/{queryId}/result")
    public ResponseEntity<Page<MovieDetail>> fetchResult(
        @PathVariable(value = "queryId") String queryId,
        @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
        @RequestParam(value = "pageSize", defaultValue = "50") int pageSize) throws AppException {
        return ResponseEntity.success(queryService.readQueryResult(queryId, pageNo, pageSize));
    }
}
