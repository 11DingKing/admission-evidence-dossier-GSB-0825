insert into source_registry (source_id, display_name, provider) values
    ('GD_EDU_EXAM',     '广东省教育考试院',           'PUBLICATION_TABLE'),
    ('SZPU_ADMISSION',  '深圳职业技术大学本科招生网', 'PUBLICATION_TABLE'),
    ('CHSI_GGKG',       '阳光高考信息平台',           'PUBLICATION_TABLE'),
    ('WHPU_ADMISSION',  '武汉职业技术大学招生网',     'PUBLICATION_TABLE');

insert into dossier (dossier_id, province_code, admission_year, subject_category, school_code, major_group_code, expected_scale) values
    ('11111111-1111-4111-8111-111111111111', '44', 2025, 'PHYSICS',       '11113', '203', 'GAOKAO_750_TENTH'),
    ('22222222-2222-4222-8222-222222222222', '42', 2025, 'ART_COMPOSITE', '10834', 'Y01', 'ART_COMPOSITE_TENTH');

insert into dossier_source (dossier_id, source_id, position) values
    ('11111111-1111-4111-8111-111111111111', 'GD_EDU_EXAM',    0),
    ('11111111-1111-4111-8111-111111111111', 'SZPU_ADMISSION', 1),
    ('11111111-1111-4111-8111-111111111111', 'CHSI_GGKG',      2),
    ('22222222-2222-4222-8222-222222222222', 'CHSI_GGKG',      0),
    ('22222222-2222-4222-8222-222222222222', 'WHPU_ADMISSION', 1);

insert into source_publication (source_id, province_code, admission_year, subject_category, school_code, major_group_code,
                                source_revision, score_tenths, score_scale, source_doc_no, effective_from, recorded_at, raw_text)
values
    ('GD_EDU_EXAM', '44', 2025, 'PHYSICS', '11113', '203', 1, 6100, 'GAOKAO_750_TENTH',
     '粤教考函〔2025〕37号', date '2025-07-19', timestamptz '2025-07-19T09:00:00Z',
     '广东省2025年普通本科批次投档情况公告（粤教考函〔2025〕37号）：深圳职业技术大学 普通物理类 专业组203 投档最高分610.0，最低分587.0，投档人数42人。各院校专业组投档分数线以公告附件为准。'),
    ('GD_EDU_EXAM', '44', 2025, 'PHYSICS', '11113', '203', 2, 6090, 'GAOKAO_750_TENTH',
     '粤教考函〔2025〕41号', date '2025-07-25', timestamptz '2025-07-25T10:00:00Z',
     '广东省2025年普通本科批次投档情况更正公告（粤教考函〔2025〕41号）：因数据校对有误，现更正深圳职业技术大学 普通物理类 专业组203 投档最高分，原公告610.0 更正为 609.0，其余数据不变，特此更正。'),
    ('SZPU_ADMISSION', '44', 2025, 'PHYSICS', '11113', '203', 1, 6090, 'GAOKAO_750_TENTH',
     '深职大招〔2025〕18号', date '2025-07-20', timestamptz '2025-07-20T08:30:00Z',
     '深圳职业技术大学本科招生网2025年录取结果公示（深职大招〔2025〕18号）：广东省普通本科批次物理类专业组203录取最高分609.0，最低分587.0。录取结果以广东省教育考试院投档数据为准。'),
    ('CHSI_GGKG', '42', 2025, 'ART_COMPOSITE', '10834', 'Y01', 1, 6044, 'ART_COMPOSITE_TENTH',
     'GGKG-2025-HB-ART-0612', date '2025-07-22', timestamptz '2025-07-22T12:00:00Z',
     '阳光高考信息平台湖北省2025年艺术类本科批投档信息（GGKG-2025-HB-ART-0612）：武汉职业技术大学 数字媒体艺术 艺术综合分604.4，为该校艺术类专业组Y01投档最高分。'),
    ('WHPU_ADMISSION', '42', 2025, 'ART_COMPOSITE', '10834', 'Y01', 1, 6044, 'ART_COMPOSITE_TENTH',
     '武职大招〔2025〕22号', date '2025-07-23', timestamptz '2025-07-23T09:30:00Z',
     '武汉职业技术大学招生网2025年湖北省艺术类本科批录取公告（武职大招〔2025〕22号）：数字媒体艺术专业（专业组Y01）录取艺术综合分最高604.4，最低581.5，录取人数26人。');
