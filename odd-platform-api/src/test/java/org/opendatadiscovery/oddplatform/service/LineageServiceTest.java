package org.opendatadiscovery.oddplatform.service;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatadiscovery.oddplatform.api.contract.model.DataEntityLineageEdge;
import org.opendatadiscovery.oddplatform.api.contract.model.DataEntityLineageNode;
import org.opendatadiscovery.oddplatform.dto.DataEntityDimensionsDto;
import org.opendatadiscovery.oddplatform.dto.lineage.LineageStreamKind;
import org.opendatadiscovery.oddplatform.mapper.DataEntityMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.DataEntityRunMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.DataSourceMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.DatasetFieldApiMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.DatasetVersionMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.DateTimeMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.LabelMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.LineageMapper;
import org.opendatadiscovery.oddplatform.mapper.LineageMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.MetadataFieldMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.MetadataFieldValueMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.NamespaceMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.OwnerMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.OwnershipMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.TagMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.TermMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.TitleMapperImpl;
import org.opendatadiscovery.oddplatform.mapper.TokenMapperImpl;
import org.opendatadiscovery.oddplatform.model.tables.pojos.DataEntityPojo;
import org.opendatadiscovery.oddplatform.model.tables.pojos.LineagePojo;
import org.opendatadiscovery.oddplatform.repository.reactive.ReactiveDataEntityRepository;
import org.opendatadiscovery.oddplatform.repository.reactive.ReactiveGroupEntityRelationRepository;
import org.opendatadiscovery.oddplatform.repository.reactive.ReactiveLineageRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for LineageService")
class LineageServiceTest {

    private final LineageMapper lineageMapper = new LineageMapperImpl();
    private LineageService lineageService;
    @Mock
    private ReactiveLineageRepository lineageRepository;
    @Mock
    private ReactiveGroupEntityRelationRepository groupEntityRelationRepository;
    @Mock
    private ReactiveDataEntityRepository dataEntityRepository;

    @BeforeEach
    void setUp() {
        lineageService = new LineageServiceImpl(lineageRepository, dataEntityRepository, groupEntityRelationRepository,
            lineageMapper);
        final TermMapperImpl termMapper = new TermMapperImpl(
            new NamespaceMapperImpl(),
            new DateTimeMapperImpl(),
            new OwnershipMapperImpl(
                new OwnerMapperImpl(),
                new TitleMapperImpl()
            )
        );
        lineageMapper.setDataEntityMapper(
            new DataEntityMapperImpl(
                new DataSourceMapperImpl(
                    new NamespaceMapperImpl(),
                    new TokenMapperImpl(
                        new DateTimeMapperImpl()
                    )
                ),
                new OwnershipMapperImpl(
                    new OwnerMapperImpl(),
                    new TitleMapperImpl()
                ),
                new TagMapperImpl(),
                new MetadataFieldValueMapperImpl(
                    new MetadataFieldMapperImpl()
                ),
                new DatasetVersionMapperImpl(
                    new DatasetFieldApiMapperImpl(
                        new LabelMapperImpl(),
                        new MetadataFieldValueMapperImpl(new MetadataFieldMapperImpl()),
                        termMapper
                    ),
                    new DateTimeMapperImpl()
                ),
                new DataEntityRunMapperImpl(
                    new DateTimeMapperImpl()
                ),
                termMapper,
                new DateTimeMapperImpl()
            )
        );
        lineageMapper.setDataSourceMapper(
            new DataSourceMapperImpl(
                new NamespaceMapperImpl(),
                new TokenMapperImpl(new DateTimeMapperImpl())
            )
        );
    }

    @Test
    void getLineageTest() {
        final var rootEntityOddrn = "root";
        final var firstChildEntityOddrn = "firstChild";
        final var secondChildEntityOddrn = "secondChild";
        final var rootEntity = new DataEntityPojo().setOddrn(rootEntityOddrn).setId(1L)
            .setEntityClassIds(new Integer[] {1});
        final var firstChildEntity = new DataEntityPojo().setId(2L).setOddrn(firstChildEntityOddrn);
        final var secondChildEntity = new DataEntityPojo().setId(3L).setOddrn(secondChildEntityOddrn);
        final var rootToFirstEntityLineage = new LineagePojo(rootEntityOddrn, firstChildEntityOddrn, null);
        final var rootToSecondEntityLineage = new LineagePojo(rootEntityOddrn, secondChildEntityOddrn, null);
        final var dto = DataEntityDimensionsDto.dimensionsBuilder()
            .dataEntity(rootEntity).build();

        when(dataEntityRepository.getDataEntityWithDataSourceAndNamespace(eq(1L))).thenReturn(Mono.just(dto));
        when(lineageRepository.getLineageRelations(any(), any(), any()))
            .thenReturn(Flux.fromStream(Stream.of(rootToFirstEntityLineage, rootToSecondEntityLineage)));
        when(lineageRepository.getLineageRelationsForDepthOne(any(), any()))
            .thenReturn(Flux.empty());
        when(groupEntityRelationRepository.fetchGroupRelations(any()))
            .thenReturn(Mono.just(new HashMap<>()));
        when(lineageRepository.getChildrenCount(any())).thenReturn(Mono.just(new HashMap<>()));
        when(lineageRepository.getParentCount(any())).thenReturn(Mono.just(new HashMap<>()));
        when(dataEntityRepository.getDataEntitiesWithDataSourceAndNamespace(any())).thenReturn(Flux.just(
            dto,
            DataEntityDimensionsDto.dimensionsBuilder().dataEntity(firstChildEntity).build(),
            DataEntityDimensionsDto.dimensionsBuilder().dataEntity(secondChildEntity).build()
        ));

        lineageService
            .getLineage(1L, 1, List.of(), LineageStreamKind.DOWNSTREAM)
            .as(StepVerifier::create)
            .assertNext(r -> {
                    assertThat(r.getDownstream().getNodes().size()).isEqualTo(3);
                    assertThat(r.getDownstream().getNodes().stream().map(DataEntityLineageNode::getId).collect(
                        Collectors.toSet())).isEqualTo(Set.of(1L, 2L, 3L));
                    assertThat(r.getDownstream().getEdges()).isEqualTo(
                        List.of(new DataEntityLineageEdge().sourceId(1L).targetId(2L),
                            new DataEntityLineageEdge().sourceId(1L).targetId(3L)));
                    }
            )
            .verifyComplete();
    }
}