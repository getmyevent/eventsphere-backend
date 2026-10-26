-- =============================================================================
-- Flyway Migration: V14__update_exhibitor_cloudinary_logos.sql
-- Update all exhibitor logos to official Cloudinary hosted asset URLs
-- =============================================================================

UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696040/01-mpsedc.jpg' WHERE company LIKE '%MPSEDC%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696048/03-truesilicon.jpg' WHERE company LIKE '%TrueSilicon%' OR company LIKE '%Truechip%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696053/04-stemrobo.jpg' WHERE company LIKE '%Stemrobo%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696058/05-cdac.jpg' WHERE company LIKE '%CDAC%' OR company LIKE '%C-DAC%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696063/06-andgate-informatics.jpg' WHERE company LIKE '%AndGate%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696068/07-scl-mohali.jpg' WHERE company LIKE '%SCL Mohali%' OR company LIKE '%SCL%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696072/08-nanodroits-technology.jpg' WHERE company LIKE '%Nanodroits%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696077/09-mcte.jpg' WHERE company LIKE '%MCTE%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696082/10-rackbank-datacenters-pvt-ltd.jpg' WHERE company LIKE '%RackBank%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696087/11-hlbs.jpg' WHERE company LIKE '%HLBS%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696092/12-spintronics-ai.jpg' WHERE company LIKE '%Spintronics%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696096/13-svvv-indore.jpg' WHERE company LIKE '%SVVV%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696101/14-vlsi-experts.jpg' WHERE company LIKE '%VLSI Experts%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696106/15-vlsi-for-all-private-ltd.jpg' WHERE company LIKE '%VLSI for all%' OR company LIKE '%Futurewiz%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696110/16-siliciom-semiconductors.jpg' WHERE company LIKE '%Siliciom%' OR company LIKE '%Impute%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696115/17-paras-semi.jpg' WHERE company LIKE '%Paras Semi%' OR company LIKE '%Paras%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696119/18-digicomm-semi.jpg' WHERE company LIKE '%DigiComm%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696124/19-vit-bhopal.jpg' WHERE company LIKE '%VIT Bhopal%' OR company LIKE '%VIT%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696128/20-manit-bhopal.jpg' WHERE company LIKE '%MANIT%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696133/21-medicaps.jpg' WHERE company LIKE '%Medicaps%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696138/22-sgsits-indore.jpg' WHERE company LIKE '%SGSITS%' OR company LIKE '%SGSTS%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696142/23-abv-iiitm-gwalior.jpg' WHERE company LIKE '%IIITM%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696147/24-jetwave.jpg' WHERE company LIKE '%Jetwave%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696152/25-nielit.jpg' WHERE company LIKE '%NIELIT%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696156/26-bio-line-india.jpg' WHERE company LIKE '%Bio Line%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696161/27-craftifai.jpg' WHERE company LIKE '%CraftifAI%' OR company LIKE '%CraftAI%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696166/28-rrcat.jpg' WHERE company LIKE '%RRCAT%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696170/29-new-age-instruments-and-material-s-pvt-ltd.jpg' WHERE company LIKE '%New Age%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696174/30-acropolis-institute-of-tecnology-and-resea.jpg' WHERE company LIKE '%Acropolis%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696180/31-sagar-group-of-institutions-sistec-bhopal.jpg' WHERE company LIKE '%Sagar%' OR company LIKE '%SISTec%' OR company LIKE '%Samvit%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696184/32-setu-manufacturing-solutions.jpg' WHERE company LIKE '%Setu%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696188/33-htl-co-india-pvt-ltd.jpg' WHERE company LIKE '%HTL%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696192/34-indiesemic-private-limited.jpg' WHERE company LIKE '%Indiesemic%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696197/35-laxmishri-printed-circuit-boad.jpg' WHERE company LIKE '%Laxmishri%' OR company LIKE '%Lakshmishri%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696210/36-sage-university-indore.jpg' WHERE company LIKE '%SAGE%' OR company LIKE '%SAGUNE%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696214/37-csir-ceeri-pilani.jpg' WHERE company LIKE '%CEERI%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696229/38-teradyne.jpg' WHERE company LIKE '%Teradyne%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696236/39-iiti-drishti-cps-foundation.jpg' WHERE company LIKE '%DRISHTI%' OR company LIKE '%AgriHub%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696241/41-nsdcs-lab-iit-indore.jpg' WHERE company LIKE '%NSDCS%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696246/42-leadsoc-technologies.jpg' WHERE company LIKE '%LeadSoc%' OR company LIKE '%Mehtab%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696250/43-daksh-instruments-pvt-ltd.jpg' WHERE company LIKE '%Daksh%' OR company LIKE '%Scientech%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696255/44-iet-davv-indore.jpg' WHERE company LIKE '%IET-DAVV%' OR company LIKE '%DAVV%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696260/45-iet-university-bhopal.jpg' WHERE company LIKE '%IET University%' OR company LIKE '%IES University%' OR company LIKE '%Oriental%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696265/46-analog-devices.jpg' WHERE company LIKE '%Analog Devices%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696270/iiser-bhopal.png' WHERE company LIKE '%IISER%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696274/nirji-semicon.jpg' WHERE company LIKE '%Nirji%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696279/prestige-piemr.svg' WHERE company LIKE '%Prestige%' OR company LIKE '%PIEMR%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696284/technisum.png' WHERE company LIKE '%Technisium%' OR company LIKE '%PinE%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696288/triveni.png' WHERE company LIKE '%TRIVENI%' OR company LIKE '%Pisarv%';
UPDATE exhibitors SET logo_url = 'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792696293/tulshree.png' WHERE company LIKE '%TULSHREE%' OR company LIKE '%Shubham%';
